# agent模块

跟踪代理人, 负责采集调用数据

# agent的实现

## Tracer -- 系统跟踪器, 跟踪入口

1. 基类 `ITracer`

我们来看看其基类 `ITracer`, 一窥概貌

```
package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 系统跟踪类
 * 都需要创建trace才能操作其他api
 * 用#号前缀来标识发起人的service
 *
 * 在client
 * 1. 第一次创建trace, 即可创建 rootspan, 其parentid为null, 记录为后面span的parentspan -- http处理/定时任务处理
 * 2. 后面创建span, 都以rootspan作为parent -- rpc调用
 *
 * 在rpc server
 * 1 收到请求时, 第一次创建trace, 需要从请求中获得parentSpan
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-29 6:19 PM
 */
abstract class ITracer {

    /**
     * 是否取样
     */
    public var isSample: Boolean? = null

    /**
     * traceId
     */
    public var id: Long = -1


    /**
     * 父span: 可能是发起人, 也可能是服务端
     */
    public var parentSpan: Span? = null

    /**
     * 新建发起人的span
     *
     * @param func
     * @return
     */
    public fun startInitiatorSpanner(func: KFunction<*>): ISpanner {
        return startInitiatorSpanner(func.javaMethod!!)
    }

    /**
     * 新建发起人的span
     *
     * @param method
     * @return
     */
    public fun startInitiatorSpanner(method: Method): ISpanner {
        return startInitiatorSpanner(method.declaringClass.name, method.getSignature())
    }

    /**
     * 新建发起人的span
     *
     * @param serviceName
     * @param name
     * @return
     */
    public abstract fun startInitiatorSpanner(serviceName: String, name: String): ISpanner

    /**
     * 新建客户端的span
     *
     * @param req
     * @return
     */
    public abstract fun startClientSpanner(req: IRpcRequest): ISpanner

    /**
     * 新建服务端的span
     *
     * @param req
     * @return
     */
    public abstract fun startServerSpanner(req: IRpcRequest): ISpanner
}
```

2. 实现类 `Tracer`

在一次调用内, 业务可能发起了好几次对其他服务的调用, 因此需要使用 `ThreadLocal` 来保存当前的 `Tracer` 对象, 在一次调用内都是有效的


使用 `Tracer.current()` 来获得当前 `Tracer` 对象, 例如

```
val spanner = Tracer.current().startServerSpanner(req)
```

3. 真正的调用记录交给 `ISpanner`

## ISpanner -- span跟踪器

### ISpanner 接口

2个属性:
1. `Tracer` 实例, 主要是在 `end()` 请求结束时, 清理 `Tracer` 实例, 以为 `Tracer` 实例的生命周期就是在当前调用过程内
2. `Span` 实例, 主要用于在  `start()` /`end()`  中添加cs/sr/ss/cr annotation

2个方法:
1. `start()` 记录调用的开始, 实现就是添加开始的annotation
2. `end()` 记录调用的结束, 实现就是添加结束的annotation

```
package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

/**
 * span跟踪器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
abstract class ISpanner(public val tracer: Tracer, public val span: Span){

	/**
	 * 开始跟踪
	 */
	public abstract fun start()

	/**
	 * 结束跟踪
	 * @param ex
	 * @return
	 */
	public abstract fun end(ex: Throwable? = null): CompletableFuture<Unit>

}
```

### span模型

1. `Span`

追踪服务调基本结构，表示跨服务的一次调用；多span形成树形结构，组合成一次Trace追踪记录。

2. `Annotation`

在span中的标注点，记录整个span时间段内发生的事件。

而`Annotation`类型有:
> - `Cs` CLIENT_SEND，客户端发起请求
> - `Cr` CLIENT_RECIEVE，客户端收到响应
> - `Sr` SERVER_RECIEVE，服务端收到请求
> - `Ss` SERVER_SEND，服务端发送结果
> - `Ex` Exception 记录异常事件

`ISpanner`的实现会在 `start()` /`end()`  中添加cs/sr/ss/cr annotation

### ISpanner 实现

```
ISpanner
	ClientSpanner -- 客户端span跟踪器
		InitiatorSpanner -- 发起人span跟踪器
	ServerSpanner -- 服务端span跟踪器
	EmptySpanner -- 啥不都干的span跟踪器
```

1. `ClientSpanner` -- 客户端span跟踪器

`start()`: 添加cs annotation

`end()`: 添加cr annotation, 并上传

2. `InitiatorSpanner` -- 发起人span跟踪器

继承 `ClientSpanner`

代表调用链的起点, 如http server处理请求

代表完整的本次调用, 也就代表当前Tracer实例的生命周期, 因此在`end()`时清理当前`Tracer`实例

3. `ServerSpanner` -- 服务端span跟踪器

`start()`: 添加sr annotation

`end()`: 添加ss annotation, 并上传

代表完整的本次调用, 也就代表当前Tracer实例的生命周期, 因此在`end()`时清理当前`Tracer`实例

4. `EmptySpanner` -- 啥不都干的span跟踪器

不采样时使用他来兼容ISpanner的调用

5. 其他

对于采集率: 与CAT/hydra类似。支持自适应采样，规则简单，对于每秒钟的请求次数进行统计，如果超过100，就按照10%的比率进行采样。

最后, 每个spanner采集调用数据后, 都调用`collectorService.send(spans)`来上传跟踪数据

# agent在3端的插件

jksoa-tracer在 rpc client/rpc server/http server等3端都做了插件, 其实现是使用对应的拦截器来埋点跟踪.

1. rpc client

插件: `RpcClientTracerPlugin`

拦截器: `RpcClientTraceInterceptor`

2. rpc server

插件: `RpcServerTracerPlugin`

拦截器: `RpcServerTraceInterceptor`

3. http server

插件 `HttpServerTracerPlugin`

拦截器 `HttpServerTraceInterceptor`