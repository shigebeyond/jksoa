package net.jkcode.jksoa.tracer.jaeger.interceptor

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.tracer.jaeger.Tracer
import net.jkcode.jksoa.tracer.jaeger.end
import net.jkcode.jkutil.common.trySupplierFuture
import java.util.concurrent.CompletableFuture

/**
 * 客户端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-06-08 2:53 PM
 */
class RpcClientTraceInterceptor: IRpcRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: IRpcRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 前置处理 -- 可以直接抛异常, 可以直接return
        val spanner = Tracer.current().startClientSpanner(req)

        // 转future
        val future = trySupplierFuture(action)

        // 后置处理
        return future.whenComplete{ r, ex ->
            spanner.end(ex)
        }
    }

}