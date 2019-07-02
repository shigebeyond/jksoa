package net.jkcode.jksoa.tracer.common.service

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

/**
 * 查询服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
interface IQueryService {

    // 根据耗时来查询跟踪信息
    fun getTracesByDuration(serviceId: Int, startTime: Long, limit: Int, durationMin: Int, durationMax: Int): JSONArray

    // 根据异常来查询跟踪信息
    fun getTracesByEx(serviceId: String, startTime: Long, limit: Int): JSONArray

    // 查询跟踪详细信息
    fun getTraceInfo(traceId: Long): JSONObject
}
