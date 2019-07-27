# server启动流程

## server启动入口

参考 `IRpcServer.start()` 的代码

```
    /**
     * 启动服务器
     * @param callback 启动后回调
     */
    fun start(callback: (() -> Unit)? = null){
        // 启动服务器
        try{
            serverLogger.debug(" ------ start rpc server ------ ")
            serverLogger.info("{}在地址[{}]上启动", name, serverUrl)
            server = this
            // 可能阻塞，只能在最后一句执行
            doStart() {
                //启动后，主动调用 ProviderLoader.load() 来扫描加载Provider服务
                ProviderLoader.load()

                // 初始化插件
                for(p in plugins)
                    p.start()

                // 调用回调
                callback?.invoke()

                // 关机时要关闭
                ClosingOnShutdown.addClosing(this)
            }
        }catch(e: Exception){
            serverLogger.error("${name}在地址[$serverUrl]上启动失败", e)
            throw RpcServerException(e)
        }
    }
```

## server启动流程

1. 启动rmi/netty的server

2. 调用 `ProviderLoader.load()` 来扫描加载Provider服务

3. 初始化插件, 插件配置在 `plugin.yaml`

4. 调用回调, 只在手动调用 `IRpcServer.start()` 时才会有回调

5. 添加关闭的钩子, 关闭当前server