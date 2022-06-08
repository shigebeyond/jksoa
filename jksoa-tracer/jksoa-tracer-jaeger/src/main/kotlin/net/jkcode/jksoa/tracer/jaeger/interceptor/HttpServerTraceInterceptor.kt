package net.jkcode.jksoa.tracer.jaeger.interceptor

import net.jkcode.jkutil.common.trySupplierFuture
import net.jkcode.jkmvc.http.HttpRequest
import net.jkcode.jkmvc.http.IHttpRequestInterceptor
import net.jkcode.jksoa.tracer.jaeger.Tracer
import net.jkcode.jksoa.tracer.jaeger.end
import java.util.concurrent.CompletableFuture

/**
 * 服务端的http请求拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-06-08 2:53 PM
 */
class HttpServerTraceInterceptor: IHttpRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     *
     * @return
     */
    public override fun intercept(req: HttpRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 前置处理 -- 可以直接抛异常, 可以直接return
        val spanner = Tracer.current().startInitiatorSpanner(req.controllerClass.clazz.qualifiedName!!, req.action + "()", req.parameterMap)

        // 转future
        val future = trySupplierFuture(action)

        // 后置处理
        return future.whenComplete{ r, ex ->
            spanner.end(ex)
        }
    }

}