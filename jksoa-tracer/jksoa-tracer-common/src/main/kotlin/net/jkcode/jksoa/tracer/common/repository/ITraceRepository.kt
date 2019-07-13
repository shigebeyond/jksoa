package net.jkcode.jksoa.tracer.common.repository

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import net.jkcode.jksoa.tracer.common.entity.tracer.Span

/**
 * 跟踪的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
interface ITraceRepository {

    /**
     * 保存收到的span
     * @param data
     */
    fun saveSpans(data: List<List<Span>>)

    /**
     * 根据耗时来查询跟踪信息
     *
     * @param serviceId 服务id
     * @param startTime 开始时间
     * @param limit 分页数
     * @param durationMin 耗时区间
     * @param durationMax
     * @return
     */
    fun getTracesByDuration(serviceId: Int, startTime: Long, limit: Int, durationMin: Int, durationMax: Int): JSONArray

    /**
     * 根据异常来查询跟踪信息
     * @param serviceId 服务id
     * @param startTime 开始时间
     * @param limit 分页数
     * @return
     */
    fun getTracesByEx(serviceId: Int, startTime: Long, limit: Int): JSONArray

    /**
     * 查询跟踪详细信息
     * @param traceId
     * @return
     */
    fun getTraceInfo(traceId: Long): JSONObject
}
