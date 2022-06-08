package net.jkcode.jksoa.tracer.jaeger.interceptor

import net.jkcode.jkutil.common.trySupplierFuture
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.tracer.jaeger.Tracer
import java.util.concurrent.CompletableFuture

/**
 * 服务端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcServerTraceInterceptor: IRpcRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: IRpcRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 前置处理 -- 可以直接抛异常, 可以直接return
        val spanner = Tracer.current().startServerSpanner(req)

        // 转future
        val future = trySupplierFuture(action)

        // 后置处理
        return future.whenComplete{ r, ex ->
            spanner.end(ex)
        }
    }

}