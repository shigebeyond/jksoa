# client初始化流程

## client的初始化入口

`RpcRequestDispatcher`作为请求的唯一出口, 因此client的初始化也放在这里

参考 `RpcRequestDispatcher.init` 代码

```
init {
    // 延迟扫描加载Referer服务
    RefererLoader.load()

    // 初始化插件
    PluginLoader.loadPlugins()
}
```

## client初始化流程

1. `RefererLoader.load()` 来扫描加载识别服务

自动扫描 `rpc-client.yaml` 配置文件中的 `servicePackages` 指定的服务包, 找到有注解`@RemoteService`的`接口类`, 并创建服务引用者`Referer`

2. 初始化插件, 插件配置在 `plugin.list`
