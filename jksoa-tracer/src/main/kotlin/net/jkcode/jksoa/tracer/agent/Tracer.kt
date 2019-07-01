package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkmvc.common.generateId
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.getServiceClass
import net.jkcode.jksoa.tracer.agent.spanner.ClientSpanner
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner
import net.jkcode.jksoa.tracer.agent.spanner.InitiatorSpanner
import net.jkcode.jksoa.tracer.agent.spanner.ServerSpanner
import net.jkcode.jksoa.tracer.common.entity.Span
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 系统跟踪类
 * 都需要创建trace才能操作其他api
 *
 * 在client
 * 1. 第一次创建trace, 即可创建 rootspan, 其parentid为null, 记录为后面span的parentspan -- http处理/定时任务处理
 * 2. 后面创建span, 都以rootspan作为parent -- rpc调用
 *
 * 在rpc server
 * 1 收到请求时, 第一次创建trace, 需要从请求中获得parentSpan
 *
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-29 6:19 PM
 */
class Tracer protected constructor() {

    companion object {

        /**
         * 取样器
         */
        protected val sampler = BaseSample()

        /**
         * 线程安全的跟踪器对象缓存
         */
        protected val tracers:ThreadLocal<Tracer> = ThreadLocal.withInitial {
            Tracer()
        }

        /**
         * 获得当前跟踪器
         */
        @JvmStatic
        public fun current(): Tracer {
            return tracers.get();
        }
    }

    /**
     * 是否取样
     */
    protected var isSample: Boolean? = null

    /**
     * id
     */
    protected var id: Long = -1


    /**
     * 父span
     */
    protected var parentSpan: Span? = null

    /**
     * 新建发起人的span
     *
     * @param func
     * @return
     */
    public fun startInitiatorSpanSpanner(func: KFunction<*>): ISpanner {
        return startInitiatorSpanSpanner(func.javaMethod!!)
    }

    /**
     * 新建发起人的span
     *
     * @param method
     * @return
     */
    public fun startInitiatorSpanSpanner(method: Method): ISpanner {
        return startInitiatorSpanSpanner(method.getServiceClass().name, method.getSignature())
    }

    /**
     * 新建发起人的span
     *
     * @param serviceId
     * @param name
     * @return
     */
    public fun startInitiatorSpanSpanner(serviceId: String, name: String): ISpanner {
        // 初始化取样 + id
        if(isSample == null) {
            isSample = sampler.isSample()
            this.id = generateId("tracer")
        }

        // 创建span
        val span = Span()
        span.id = generateId("span")
        span.service = serviceId;
        span.name = name
        span.traceId = id

        // 发起人, 作为当前线程的后续span的父span
        parentSpan = span

        return InitiatorSpanner(this, span).apply { start() }
    }

    /**
     * 新建服务端的span
     *
     * @param req
     * @return
     */
    public fun startServerSpanSpanner(req: IRpcRequest): ISpanner {
        // 初始化取样 + id : 根据请求的附加参数来确定
        val traceId: Long? = req.getAttachment("traceId")
        if(traceId == null) { // 不采样
            isSample = false
            return ISpanner.EmptySpanner
        }
        this.id = traceId

        // 创建span
        val span = Span()
        span.id = req.getAttachment("spanId")!!
        span.parentId = req.getAttachment("parentId")!!
        span.service = req.serviceId;
        span.name = req.methodSignature
        span.traceId = id

        // server端receive, 作为当前线程的后续span的父span
        parentSpan = span

        return ServerSpanner(this, span).apply { start() }
    }

    /**
     * 新建客户端的span
     *
     * @param req
     * @return
     */
    public fun startClientSpanSpanner(req: IRpcRequest): ISpanner {
        if(!isSample!!) // 不取样
            return ISpanner.EmptySpanner

        // 创建span
        val span = Span()
        span.id = generateId("span")
        span.service = req.serviceId;
        span.name = req.methodSignature
        span.traceId = id

        // 父span
        if(parentSpan != null)
            span.parentId = parentSpan!!.id

        // 向下游传递的参数
        req.setAttachment("spanId", span.id)
        req.setAttachment("parentId", span.parentId)
        req.setAttachment("traceId", span.traceId)

        return ClientSpanner(this, span).apply { start() }
    }

    /**
     * 清理
     */
    internal fun clear(){
        tracers.remove()
    }

}