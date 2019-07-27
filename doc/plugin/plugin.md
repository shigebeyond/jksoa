# 概述

引入了插件机制, 方便扩展系统功能

jksoa将系统抽象为3个端: rpc-client端, rpc-server端, http-server端

分别对应有3类插件:

1. rpcClientPlugins: rpc客户端的插件, 在rpc server启动 `net.jkcode.jksoa.server.IRpcServer.start()` 时调用

2. rpcServerPlugins: rpc服务端的插件, 在rpc client的请求分发者初始化 `net.jkcode.jksoa.client.dispatcher.RpcRequestDispatcher.init()` 时调用

3. httpServerPlugins: http服务端的插件, 在http filter初始化 `net.jkcode.jkmvc.http.JkFilter.init()` 时调用

插件接口是 `net.jkcode.jkmvc.common.IPlugin`, 主要有 `start()` 方法, 在3端启动时调用, 从而增加自定义的逻辑处理.

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

参考tracer组件中的 `net.jkcode.jksoa.tracer.agent.plugin.RpcClientTracerPlugin`

```
package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.tracer.agent.interceptor.RpcClientTraceInterceptor

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class RpcClientTracerPlugin: `IPlugin` {

    /**
     * 初始化
     */
    override fun doStart() {
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientTraceInterceptor())
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}
```

## 应用自定义插件

应用哪些插件是配置在 plugin.yaml 中

参考tracer组件中的插件配置: jksoa/jksoa-tracer/jksoa-tracer-agent/src/test/resources/plugin.yaml

```
# rpc客户端的插件
rpcClientPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.RpcClientTracerPlugin
# rpc服务端的插件
rpcServerPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.RpcServerTracerPlugin
# http服务端的插件
httpServerPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.HttpServerTracerPlugin
```