package net.jkcode.jksoa.tracer.collector.service

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.orm.collectColumn
import net.jkcode.jkmvc.orm.toMap
import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.entity.tracer.Annotation
import net.jkcode.jksoa.tracer.common.entity.service.Service
import net.jkcode.jksoa.tracer.common.entity.tracer.Trace
import net.jkcode.jksoa.tracer.common.model.service.AppModel
import net.jkcode.jksoa.tracer.common.model.service.ServiceModel
import net.jkcode.jksoa.tracer.common.model.tracer.AnnotationModel
import net.jkcode.jksoa.tracer.common.model.tracer.SpanModel
import net.jkcode.jksoa.tracer.common.model.tracer.TraceModel
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService

import java.util.concurrent.CompletableFuture

/**
 * 基于orm实现的collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmCollectorService : ICollectorService {

    /**
     * 同步服务
     *    1 只新增, 不删除
     *    2 返回所有应用的service, 用于获得service对应的id, 给span引用, 存储时省点空间
     *
     * @param appName 应用名
     * @param serviceNames 服务全类名
     * @return 所有应用的service的name对id的映射
     */
    public override fun syncServices(appName: String, serviceNames: List<String>): Map<String, Int> {
        // 查询app
        var app = AppModel.queryBuilder().where("name", Application.name).findModel<AppModel>()
        if(app == null){
            // 新建app
            app = AppModel()
            app.name = Application.name
            app.create()
        }
        val appId = app!!.id

        // 查询service
        val oldServices = ServiceModel.queryBuilder().where("app_id", app.id).findAllModels<ServiceModel>()
        val oldServiceNames = oldServices.collectColumn("name")

        // 新建service -- 只新增, 不删除
        val newServiceNames = ArrayList(serviceNames)
        newServiceNames.removeAll(oldServiceNames)
        if(newServiceNames.isNotEmpty()) {
            val newServices = newServiceNames.map { name ->
                val service = Service()
                service.appId = app.id
                service.name = name
                service
            }
            ServiceModel.batchInsert(newServices)
        }

        // 返回全部service
        return ServiceModel.queryBuilder().findAllModels<ServiceModel>().toMap<String, Int>("name", "id") as Map<String, Int>
    }

    /**
     * span队列
     */
    private val spanQueue: RequestQueueFlusher<List<Span>, Void> = object: RequestQueueFlusher<List<Span>, Void>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(spanses: List<List<Span>>, reqs: ArrayList<Pair<List<Span>, CompletableFuture<Void>>>): Boolean {
            // 保存span
            saveSpans(spanses)
            return true
        }
    }

    /**
     * collector接收agent发送过来的span
     *
     * @param spans
     * @return
     */
    public override fun send(spans: List<Span>): CompletableFuture<Void> {
        return spanQueue.add(spans)
    }

    /**
     * 遍历收到的span
     */
    inline protected fun forEachSpan(data: List<List<Span>>, action: (Span) -> Unit){
        data.forEach {
            it.forEach(action)
        }
    }

    /**
     * 保存收到的span
     */
    public fun saveSpans(data: List<List<Span>>){
        //1 保存span
        val spans = ArrayList<Span>()
        forEachSpan(data){ span ->
            if(!span.isServer) // 非server才保存
                spans.add(span)
        }
        SpanModel.batchInsert(spans)

        // 2 保存trace
        val traces = ArrayList<Trace>()
        forEachSpan(data){ span ->
            if(span.isInitiator) { // 发起人span
                val t = Trace()
                t.id = span.traceId
                t.duration = span.calculateDurationClient().toInt() // 耗时
                t.serviceId = span.serviceId
                t.timestamp = span.startTimeClient // 开始时间
                traces.add(t)
            }
        }
        TraceModel.batchInsert(traces)

        // 3 保存annotation
        val annotations = ArrayList<Annotation>()
        forEachSpan(data){ span ->
            annotations.addAll(span.annotations)
        }
        AnnotationModel.batchInsert(annotations)
    }
}