package net.jkcode.jksoa.tracer.collector.service

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.orm.toMap
import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import net.jkcode.jksoa.tracer.common.entity.Span
import net.jkcode.jksoa.tracer.common.model.AppModel
import net.jkcode.jksoa.tracer.common.model.ServiceModel
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService
import net.jkcode.jksoa.tracer.common.service.IInsertService

import java.util.concurrent.CompletableFuture

/**
 * 基于orm实现的collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmCollectorService : ICollectorService {

    private var insertService: IInsertService = OrmInsertService()


    /**
     * 同步服务
     * @param appName 应用名
     * @param serviceNames 服务全类名
     * @return appId + service的name对id的映射
     */
    public override fun syncService(appName: String, serviceNames: List<String>): CompletableFuture<Pair<Int, HashMap<String, Int>>>? {
        // 查询app
        val app = AppModel.queryBuilder().where("name", Application.name).findModel<AppModel>()
        if(app == null){
            // 新建app
            val app = AppModel()
            app.name = Application.name
            app.create()
        }
        val appId = app!!.id

        // 查询service
        val serviceMap = ServiceModel.queryBuilder().where("name", "IN", serviceNames).findAllModels<ServiceModel>().toMap<String, Int>("name", "id") as HashMap<String, Int>

        // 新建service
        (serviceNames as MutableList).removeAll(serviceMap.keys)
        for (name in serviceNames){
            val service = ServiceModel()
            service.appId = app.id
            service.name = name
            service.create()
            serviceMap.put(name, service.id)
        }

        return CompletableFuture.completedFuture(appId to serviceMap)
    }

    /**
     * span队列
     */
    private val spanQueue: RequestQueueFlusher<List<Span>, Void> = object: RequestQueueFlusher<List<Span>, Void>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(spanses: List<List<Span>>, reqs: ArrayList<Pair<List<Span>, CompletableFuture<Void>>>): Boolean {
            val spans = ArrayList<Span>()
            spanses.forEach {
                spans.addAll(it)
            }

            for (s in spans) {
                insertService.addSpan(s)
                insertService.addTrace(s)
                insertService.addAnnotation(s)
            }

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


}