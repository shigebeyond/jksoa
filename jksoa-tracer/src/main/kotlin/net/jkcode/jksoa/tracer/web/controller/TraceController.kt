package net.jkcode.jksoa.tracer.web.controller

import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.tracer.common.service.IQueryService
import net.jkcode.jksoa.tracer.web.service.OrmQueryService

/**
 * 跟踪信息查询的控制器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class TraceController: Controller()
{

    companion object {

        val queryService: IQueryService = OrmQueryService()
    }


    // 根据耗时来查询跟踪信息
    fun listAction(){
        val serviceId: Int = req["serviceId"]!!
        val startTime: Long = req["startTime"]!!
        val durationMin: Int = req["durationMin"]!!
        val durationMax: Int = req["durationMax"]!!
        val sum: Int = req["sum"]!!
        val traces = queryService.getTracesByDuration(serviceId, startTime, sum, durationMin, durationMax)
        res.renderJson(traces)
    }

    // 根据异常来查询跟踪信息
    fun listExAction(){
        val serviceId: String = req["serviceId"]!!
        val startTime: Long = req["startTime"]!!
        val sum: Int = req["sum"]!!
        val traces = queryService.getTracesByEx(serviceId, startTime, sum)
        res.renderJson(traces)
    }

    // 查询跟踪详细信息
    fun infoAction() {
        val traceId: Long = req.getRouteParameter("traceId")!!
        val info = queryService.getTraceInfo(traceId)
        res.renderJson(info)
    }
}
