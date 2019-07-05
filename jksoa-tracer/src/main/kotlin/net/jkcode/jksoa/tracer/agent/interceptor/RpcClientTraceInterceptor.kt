package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * 客户端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcClientTraceInterceptor: IRpcRequestInterceptor {

    /**
     * 前置处理
     * @param req
     * @return 调用结果作为after()调用的第二参数
     */
    override fun before(req: IRpcRequest): Any? {
        return Tracer.current().startClientSpanner(req)
    }

    /**
     * 后置处理
     * @param req 可能会需要通过req来传递before()中操作过的对象, 如
     * @param beforeResult before()方法的调用结果
     * @param result 目标方法的调用结果
     * @param ex 目标方法的调用异常
     */
    override fun after(req: IRpcRequest, beforeResult: Any?, result: Any?, ex: Throwable?) {
        val spanner = beforeResult as ISpanner
        spanner.end(ex)
    }

}