package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.collection.DoneFlagList
import net.jkcode.jkutil.ttl.SttlCurrentHolder
import net.jkcode.jkutil.common.generateId
import net.jkcode.jkutil.ttl.AllRequestScopedTransferableThreadLocal
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.tracer.agent.loader.AnnotationTraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.loader.ITraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.sample.BaseSample
import net.jkcode.jksoa.tracer.agent.spanner.*
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService
import net.jkcode.jksoa.tracer.common.tracerLogger
import java.util.*
import kotlin.collections.HashMap

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
class Tracer protected constructor() : ITracer() {

    companion object: SttlCurrentHolder<Tracer>(AllRequestScopedTransferableThreadLocal { Tracer() }) { // 所有请求域的可传递的 ThreadLocal

        /**
         * 取样器
         */
        protected val sampler = BaseSample()

        /**
         * collector服务
         *
         * 问题: Tracer 与 RcpRequestDispatcher 的循环依赖
         *      Tracer -> ICollectorService代理 -> RcpRequestDispatcher
         *      RcpRequestDispatcher -> JkTracerPlugin插件 -> Tracer
         * 解决: 1. Tracer 不能直接引用 ICollectorService
         *      2. JkTracerPlugin插件中延迟调用 ICollectorService.syncServices()
         */
        public val collectorService: ICollectorService
            get(){
                //return OrmCollectorService()
                return Referer.getRefer<ICollectorService>()
            }

        /**
         * <服务名, 服务id>
         */
        public val serviceMap:HashMap<String, Int> = HashMap()

        /**
         * 可跟踪的服务加载器
         */
        protected val serviceLoaders: DoneFlagList<ITraceableServiceLoader> by lazy { DoneFlagList<ITraceableServiceLoader>() }

        init {
            // 有@TraceableService注解的类的加载器
            addServiceLoader(AnnotationTraceableServiceLoader())
        }

        /**
         * 添加服务加载器
         */
        public fun addServiceLoader(loader: ITraceableServiceLoader) {
            serviceLoaders.add(loader)
        }

        /**
         * 同步service
         *   可重入, 已去重
         */
        @Synchronized
        public fun syncServices() {
            // 只收集未加载过的加载器的服务
            if(serviceLoaders.isAllDone())
                return
            val serviceNames: MutableList<String> = LinkedList()
            for((i, loader) in serviceLoaders.doneIterator(false)){
                serviceNames.addAll(loader.load())
                serviceLoaders.setDone(i, true)
            }

            // 去重
            if(serviceNames.isEmpty() || serviceMap.keys.containsAll(serviceNames))
                return

            // 只同步新的service
            val newServiceNames = ArrayList(serviceNames)
            newServiceNames.removeAll(serviceMap.keys)
            if(newServiceNames.isNotEmpty()) {
                // 同步service
                val serviceMap = collectorService.syncServices(JkApp.name, newServiceNames)
                this.serviceMap.putAll(serviceMap)
            }
            tracerLogger.debug("同步service: {}", serviceNames)
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
    }

    /**
     * 新建发起人的span
     *
     * @param serviceName
     * @param name
     * @return
     */
    public override fun startInitiatorSpanner(serviceName: String, name: String): ISpanner {
        // JkTracerPlugin插件中延迟调用 ICollectorService.syncServices()
        syncServices()

        // 用#号前缀来标识发起人的service
        val serviceName2 = "#$serviceName"

        // 初始化取样 + id
        if(isSample == null) {
            isSample = sampler.isSample()

            // 不取样
            if(!isSample!!)
                return EmptySpanner

            this.id = generateId("tracer")
        }

        // 创建span
        val span = Span()
        span.id = generateId("span")
        span.parentId = null // 父span为null
        span.serviceId = getServiceIdByName(serviceName2)
        span.name = name
        span.traceId = id

        // 发起人, 作为当前线程的后续span的父span
        parentSpan = span

        return InitiatorSpanner(this, span).apply { start() }
    }

    /**
     * 新建客户端的span
     *
     * @param req
     * @return
     */
    public override fun startClientSpanner(req: IRpcRequest): ISpanner {
        // 不跟踪 ICollectorService
        if(req.serviceId == ICollectorService::class.qualifiedName)
            return EmptySpanner

        // 不取样
        if(!isSample!!)
            return EmptySpanner

        // 创建span
        val span = Span()
        span.id = generateId("span") // 新开一个span, 用于记录cs/cr的annotation
        span.serviceId = getServiceIdByName(req.serviceId)
        span.name = req.methodSignature
        span.traceId = id

        // 以当前发起人/服务端的span为 父span
        if(parentSpan != null)
            span.parentId = parentSpan!!.id

        // 向下游传递跟踪信息
        attachTraceInfo(req, span)

        return ClientSpanner(this, span).apply { start() }
    }

    /**
     * 新建服务端的span
     *
     * @param req
     * @return
     */
    public override fun startServerSpanner(req: IRpcRequest): ISpanner {
        // 不跟踪 ICollectorService
        if(req.serviceId == ICollectorService::class.qualifiedName)
            return EmptySpanner

        // 创建span
        val span = Span()
        // 从rpc请求的附加参数中解析跟踪信息
        val succ = extractTraceInfo(span, req)
        if(!succ){ // 不采样
            isSample = false
            return EmptySpanner
        }

        span.serviceId = getServiceIdByName(req.serviceId)
        span.name = req.methodSignature
        this.id = span.traceId

        // server端receive, 作为当前线程的后续span的父span
        parentSpan = span

        return ServerSpanner(this, span).apply { start() }
    }

    /**
     * 向下游(server)传递跟踪信息
     *   塞到rpc请求的附加参数中
     */
    protected fun attachTraceInfo(req: IRpcRequest, span: Span) {
        req.putAttachment("spanId", span.id)
        req.putAttachment("parentId", span.parentId)
        req.putAttachment("traceId", span.traceId)
    }

    /**
     * 从rpc请求的附加参数中解析跟踪信息
     */
    protected fun extractTraceInfo(span: Span, req: IRpcRequest): Boolean {
        val traceId: Long? = req.getAttachment("traceId")
        if(traceId == null)  // 不采样
            return false

        span.traceId = traceId
        span.id = req.getAttachment("spanId")!! // 复用client的span, 不保存span, 只补充sr/ss的annotation
        span.parentId = req.getAttachment("parentId")!! // 沿用client的span的父span
        return true
    }

}