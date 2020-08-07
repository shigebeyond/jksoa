package net.jkcode.jksoa.tracer.web.controller

import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.tracer.common.repository.ITraceRepository
import net.jkcode.jksoa.tracer.common.repository.OrmTraceRepository

/**
 * 跟踪信息查询的控制器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class TraceController: Controller()
{
    companion object {

        public val repository: ITraceRepository = OrmTraceRepository()
    }


    // 根据耗时来查询跟踪信息
    fun listAction(){
        val serviceId: Int = req["serviceId"]!!
        val startTime: Long = req["startTime"]!!
        val durationMin: Int = req["durationMin"]!!
        val durationMax: Int = req["durationMax"]!!
        val sum: Int = req["sum"]!!
        val traces = repository.getTracesByDuration(serviceId, startTime, sum, durationMin, durationMax)
        res.renderJson(traces)
    }

    // 根据异常来查询跟踪信息
    fun listExAction(){
        val serviceId: Int = req["serviceId"]!!
        val startTime: Long = req["startTime"]!!
        val sum: Int = req["sum"]!!
        val traces = repository.getTracesByEx(serviceId, startTime, sum)
        res.renderJson(traces)
    }

    // 查询跟踪详细信息
    fun infoAction() {
        val traceId: Long = req["id"]!!
        val info = repository.getTraceInfo(traceId)
        res.renderJson(info)
    }
}
