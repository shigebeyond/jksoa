package net.jkcode.jksoa.tracer.common.repository

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import net.jkcode.jkmvc.util.TreeJsonFactory
import net.jkcode.jksoa.tracer.common.entity.tracer.Annotation
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.entity.tracer.Trace
import net.jkcode.jksoa.tracer.common.model.tracer.AnnotationModel
import net.jkcode.jksoa.tracer.common.model.tracer.SpanModel
import net.jkcode.jksoa.tracer.common.model.tracer.TraceModel

/**
 * 基于orm实现的跟踪仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmTraceRepository : ITraceRepository {

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
     * @param data
     */
    public override fun saveSpans(data: List<List<Span>>){
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

    /**
     * 查询跟踪详细信息
     * @param traceId
     * @return
     */
    public override fun getTraceInfo(traceId: Long): JSONObject {
        val spans = SpanModel.queryBuilder().with("annotations").where("trace_id", traceId).findAllModels<SpanModel>()

        val trace = JSONObject()

        // 是否可用
        trace["available"] = spans.all { it.isAvailable }

        // 构建span树
        val rootSpans =TreeJsonFactory<Long>("id", "parentId").buildTreeJsons(spans){
            val span = it as SpanModel
            val data = span.toMap()
            // 计算耗时
            data["durationServer"] = span.calculateDurationServer()
            data["durationClient"] = span.calculateDurationClient()
            data
        }
        // 不存在
        if(rootSpans.isEmpty())
            return trace

        // 获得根span
        val rootSpan = rootSpans.single()
        trace["rootSpan"] = rootSpan
        trace["traceId"] = rootSpan.get("traceId")

        return trace
    }


    /**
     * 根据耗时来查询跟踪信息
     * @param serviceId 服务id
     * @param startTime 开始时间
     * @param limit 分页数
     * @param durationMin 耗时区间
     * @param durationMax
     * @return
     */
    public override fun getTracesByDuration(serviceId: Int, startTime: Long, limit: Int, durationMin: Int, durationMax: Int): JSONArray {
        /*
        SELECT * FROM trace
            WHERE serviceId=#{serviceId}
            and timestamp >= #{startTime}
            and duration <= #{durationMax}
            and duration >= #{durationMin}
            limit #{limit}
         */
        val items = TraceModel.queryBuilder()
                .where("service_id", serviceId)
                .where("timestamp", ">=", startTime)
                .where("duration", "<=", durationMax)
                .where("duration", ">=", durationMin)
                .limit(limit)
                .findAllModels<TraceModel>()

        val array = JSONArray()
        for (trace in items) {
            val obj = JSONObject()
            obj["serviceId"] = trace.serviceId
            obj["timestamp"] = trace.timestamp
            obj["duration"] = trace.duration
            obj["traceId"] = trace.id.toString() // long转string, 预防js对long丢失精度
            array.add(obj)
        }
        return array
    }

    /**
     * 根据异常来查询跟踪信息
     * @param serviceId 服务id
     * @param startTime 开始时间
     * @param limit 分页数
     * @return
     */
    public override fun getTracesByEx(serviceId: Int, startTime: Long, limit: Int): JSONArray {
        /*select a.*, t.timestamp from annotation a
            left join trace t
            on a.traceId=t.traceId
            and t.time>= #{startTime}
            where a.serviceId=#{serviceId}
            and a.key = "ex"
            group by spanId
            limit #{limit}
        */
        val items = AnnotationModel.queryBuilder().with("trace")
                .where("trace.timestamp", ">=", startTime)
                .where("annotation.service_id", serviceId)
                .where("annotation.key", Annotation.EXCEPTION)
                .groupBy("span_id")
                .limit(limit)
                .findAllModels<AnnotationModel>()

        val array = JSONArray()
        for (annotation in items) {
            val obj = JSONObject()
            obj["serviceId"] = annotation.serviceId
            obj["timestamp"] = annotation.timestamp
            obj["exInfo"] = annotation.value
            obj["traceId"] = annotation.id
            array.add(obj)
        }

        return array
    }

}
