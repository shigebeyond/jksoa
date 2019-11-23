# 请求拦截器

请求拦截器, 顾名思义, 就是拦截请求的处理

jksoa将系统抽象为3个端: rpc-client端, rpc-server端, http-server端

分别对应有3类请求拦截器:

1. rpc客户端的拦截器, 配置在 `rpc-client.yaml` 的 `interceptors` 属性, 会应用到 `RpcInvocationHandler.interceptors`

2. rpc服务端的拦截器, 配置在 `rpc-server.yaml` 的 `interceptors` 属性, 会应用到 `HttpRequestHandler.interceptors`

3. http服务端的拦截器, 配置在 `http.yaml` 的 `interceptors` 属性, 会应用到 `HttpRequestHandler.interceptors`

请求拦截器接口是 `net.jkcode.jkutil.interceptor.IRequestInterceptor`, 主要有 `intercept()` 方法, 在3端的请求处理时调用, 从而增加自定义的逻辑处理.

# 自定义请求拦截器

## 实现自定义请求拦截器

1.  `IRequestInterceptor` 接口定义如下:

返回类型是 `CompletableFuture`, 1是给业务方调用, 让他能设置回调, 2是给拦截器链表的下一个拦截器调用, 让他能够更准确的确定后置处理的调用时机, 详情参考`net.jkcode.jkutil.interceptor.RequestInterceptorChain`

```
/**
 * 请求拦截器
 *    泛型 R 是请求类型
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-01 11:39 AM
 */
interface IRequestInterceptor<R> {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    fun intercept(req: R, action: () -> Any?): CompletableFuture<Any?>
}
```

2.  实现 `IRequestInterceptor` 接口

参考tracer组件中的`net.jkcode.jksoa.tracer.agent.interceptor.HttpServerTraceInterceptor`

```
package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jkmvc.http.HttpRequest
import net.jkcode.jkmvc.http.IHttpRequestInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import java.util.concurrent.CompletableFuture

/**
 * 服务端的http请求拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class HttpServerTraceInterceptor: IHttpRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: HttpRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 前置处理 -- 可以直接抛异常, 可以直接return
        val spanner = Tracer.current().startInitiatorSpanner(req.controllerClass.clazz.qualifiedName!!, req.action + "()")

        // 转future
        val future = trySupplierFuture(action)

        // 后置处理
        return future.whenComplete{ r, ex ->
            spanner.end(ex)
        }
    }

}
```

## 应用自定义请求拦截器

应用哪些请求拦截器, 是配置在对应3端的配置文件中

```
debug: true
controllerPackages:
    - net.jkcode.jkmvc.example.controller
interceptors: # 请求拦截器
    - net.jkcode.jksoa.tracer.agent.interceptor.HttpServerTraceInterceptor
```
