package net.jkcode.jksoa.tracer.jaeger

import io.opentracing.Span
import net.jkcode.jksoa.common.IRpcRequest

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
     * 父span: 可能是发起人, 也可能是服务端
     */
    public var parentSpan: Span? = null

    /**
     * 新建发起人的span
     *
     * @param serviceName
     * @param name
     * @param params
     * @return
     */
    public abstract fun startInitiatorSpanner(serviceName: String, name: String, params: Map<String, *>): Span

    /**
     * 新建客户端的span
     *
     * @param req
     * @return
     */
    public abstract fun startClientSpanner(req: IRpcRequest): Span

    /**
     * 新建服务端的span
     *
     * @param req
     * @return
     */
    public abstract fun startServerSpanner(req: IRpcRequest): Span
}