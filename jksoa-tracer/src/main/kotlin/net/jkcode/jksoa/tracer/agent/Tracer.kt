package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.common.generateId
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.tracer.agent.loader.ITraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.sample.BaseSample
import net.jkcode.jksoa.tracer.agent.spanner.ClientSpanner
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner
import net.jkcode.jksoa.tracer.agent.spanner.InitiatorSpanner
import net.jkcode.jksoa.tracer.agent.spanner.ServerSpanner
import net.jkcode.jksoa.tracer.collector.service.OrmCollectorService
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService
import net.jkcode.jksoa.tracer.tracerLogger
import java.lang.reflect.Method
import java.util.*
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
         * collector服务
         */
        //protected val collectorService: ICollectorService = Referer.getRefer<ICollectorService>()
        public val collectorService: ICollectorService = OrmCollectorService()

        /**
         * <服务名, 服务id>
         */
        public val serviceMap:HashMap<String, Int> = HashMap()

        /**
         * 可跟踪的服务加载器
         */
        protected val serviceLoaders: LinkedList<ITraceableServiceLoader> = LinkedList()

        /**
         * 同步加载器的service
         */
        public fun syncLoaderServices(loader: ITraceableServiceLoader) {
            // 去重
            if(!serviceLoaders.contains(loader)) {
                serviceLoaders.add(loader)
                // 同步service
                syncServices(loader.load())
            }
        }

        /**
         * 同步service
         */
        @Synchronized
        public fun syncServices(serviceNames: List<String>) {
            // 去重
            if(serviceMap.keys.containsAll(serviceNames))
                return

            // 只同步新的service
            val newServiceNames = ArrayList(serviceNames)
            newServiceNames.removeAll(serviceMap.keys)
            if(newServiceNames.isNotEmpty()) {
                // 同步service
                val serviceMap = collectorService.syncServices(Application.name, newServiceNames)
                this.serviceMap.putAll(serviceMap)
            }
            tracerLogger.info("同步servcie: {}", serviceNames)
        }

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
        return startInitiatorSpanSpanner(method.declaringClass.name, method.getSignature())
    }

    /**
     * 根据服务名获得id
     * @param name
     * @return id
     */
    fun getServiceIdByName(name: String): Int{
        if(!serviceMap.containsKey(name))
            throw IllegalArgumentException("不能识别服务: $name")

        return serviceMap[name]!!
    }

    /**
     * 新建发起人的span
     *
     * @param serviceName
     * @param name
     * @return
     */
    public fun startInitiatorSpanSpanner(serviceName: String, name: String): ISpanner {
        // 用#号前缀来标识发起人的service
        val serviceName2 = "#$serviceName"

        // 初始化取样 + id
        if(isSample == null) {
            isSample = sampler.isSample()
            this.id = generateId("tracer")
        }

        // 创建span
        val span = Span()
        span.id = generateId("span")
        span.serviceId = getServiceIdByName(serviceName2)
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
        // 不跟踪 ICollectorService
        if(req.serviceId == ICollectorService::class.qualifiedName)
            return ISpanner.EmptySpanner

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
        span.serviceId = getServiceIdByName(req.serviceId)
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
        // 不跟踪 ICollectorService
        if(req.serviceId == ICollectorService::class.qualifiedName)
            return ISpanner.EmptySpanner

        // 不取样
        if(!isSample!!)
            return ISpanner.EmptySpanner

        // 创建span
        val span = Span()
        span.id = generateId("span")
        span.serviceId = getServiceIdByName(req.serviceId)
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