package net.jkcode.jksoa.tracer.common.service

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import net.jkcode.jkmvc.orm.toJson
import net.jkcode.jkmvc.util.TreeNode
import net.jkcode.jksoa.tracer.common.model.SpanModel
import net.jkcode.jksoa.tracer.common.model.TraceModel

/**
 * 基于orm实现的查询服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmQueryService : IQueryService {

    // 查询跟踪详细信息
    override fun getTraceInfo(traceId: Long): JSONObject {
        val spans = SpanModel.queryBuilder().with("annotations").where("trace_id", traceId).findAllModels<SpanModel>()

        val trace = JSONObject()

        // 是否可用
        trace["available"] = spans.all { it.isAvailable }

        // 构建span树
        val rootSpans = TreeNode.buildTreeNodes<Long, SpanModel>(spans, "id", "parentId", "name"){ span ->
            val data = span.toMap()
            // 计算耗时
            data["durationServer"] = span.calculateDurationServer()
            data["durationClient"] = span.calculateDurationClient()
            data
        }
        // 获得根span
        val rootSpan = rootSpans.single()
        trace["rootSpan"] = rootSpan
        trace["traceId"] = (rootSpan.data as Map<String, *>).get("traceId")

        return trace
    }


    // 根据耗时来查询跟踪信息
    override fun getTracesByDuration(serviceId: String, startTime: Long, limit: Int, durationMin: Int, durationMax: Int): JSONArray {
        /*
        SELECT * FROM trace
            WHERE service=#{serviceId}
            and timestamp >= #{startTime}
            and duration <= #{durationMax}
            and duration >= #{durationMin}
            limit #{limit}
         */
        val items = TraceModel.queryBuilder()
                .where("service", serviceId)
                .where("timestamp", ">=", startTime)
                .where("duration", ",=", durationMax)
                .where("duration", ">=", durationMin)
                .limit(limit)
                .findAllModels<TraceModel>()

        val array = JSONArray()
        for (trace in items) {
            val obj = JSONObject()
            obj["service"] = trace.service
            obj["timestamp"] = trace.timestamp
            obj["duration"] = trace.duration
            obj["traceId"] = trace.id
            array.add(obj)
        }
        return array
    }

    // 根据异常来查询跟踪信息
    override fun getTracesByEx(serviceId: String, startTime: Long, limit: Int): JSONArray {
        /*select a.*, t.timestamp from annotation a
            left join trace t
            on a.traceId=t.traceId
            and t.time>= #{startTime}
            where a.service=#{serviceId}
            group by spanId
            limit #{limit}
        */
        val items = TraceModel.queryBuilder().with("annotations")
                .where("trace.timestamp", ">=", startTime)
                .where("annotations.service", serviceId)
                .groupBy("spanId")
                .limit(limit)
                .findAllModels<TraceModel>()

        val array = JSONArray()
        for (trace in items) {
            val obj = JSONObject()
            obj["service"] = trace.service
            obj["timestamp"] = trace.timestamp
            obj["exInfo"] = trace.annotations.first().value
            obj["traceId"] = trace.id
            array.add(obj)
        }

        return array
    }

}
