# 插件

引入了插件机制, 方便扩展系统功能

jksoa将系统抽象为3个端: rpc-client端, rpc-server端, http-server端, 而插件可以扩展3端的功能(如增加拦截器), 随意你扩展哪个端.

3端分别对应有3处初始化插件的调用, 即调用`PluginLoader.loadPlugins()`:

1. rpc客户端初始化插件: 在rpc server启动 `net.jkcode.jksoa.rpc.server.IRpcServer.start()` 时调用

2. rpc服务端初始化插件: 在rpc client的请求分发者初始化 `net.jkcode.jksoa.rpc.client.dispatcher.RpcRequestDispatcher.init()` 时调用

3. http服务端初始化插件: 在http filter初始化 `net.jkcode.jkmvc.http.JkFilter.init()` 时调用

系统保证插件只初始化一次，哪怕你3端都调用了。

插件接口是 `net.jkcode.jkutil.common.IPlugin`, 主要有 `start()` 方法, 在3端启动时调用, 从而增加自定义的逻辑处理.

# 自定义插件

## 实现自定义插件

只需要实现 `IPlugin` 接口即可

1. `IPlugin` 接口的定义如下

```
package net.jkcode.jkmvc.common

import java.io.Closeable

/**
 * 插件, 主要2个方法
 *    1 start() 初始化
 *    2 close() 关闭
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 4:57 PM
 */
interface IPlugin: Closeable {

    /**
     * 初始化
     */
    fun start(){
        commonLogger.debug(" ------ plugin: {} ------ ", this.javaClass)
        doStart()
    }

    /**
     * 初始化
     */
    fun doStart()

}
```

2. 实现 `IPlugin` 接口

参考tracer组件中的 `net.jkcode.jksoa.tracer.agent.JkTracerPlugin`

```
package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.tracer.agent.interceptor.RpcClientTraceInterceptor

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class JkTracerPlugin: `IPlugin` {

    /**
     * 初始化
     */
    override fun doStart() {
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientTraceInterceptor())
        ...
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}
```

## 应用自定义插件

应用哪些插件是配置在 plugin.list 中

参考tracer组件中的插件配置: jksoa/jksoa-tracer/jksoa-tracer-agent/src/test/resources/plugin.list

```
# rpc client/rpc server/http server等3端的插件
net.jkcode.jksoa.tracer.agent.JkTracerPlugin
```