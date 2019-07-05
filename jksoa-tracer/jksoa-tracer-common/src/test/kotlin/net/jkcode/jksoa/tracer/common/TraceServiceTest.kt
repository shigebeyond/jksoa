package net.jkcode.jksoa.tracer.common

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.tracer.common.entity.tracer.Annotation
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.model.tracer.AnnotationModel
import net.jkcode.jksoa.tracer.common.model.tracer.SpanModel
import net.jkcode.jksoa.tracer.common.model.tracer.TraceModel
import net.jkcode.jksoa.tracer.common.service.ITraceService
import net.jkcode.jksoa.tracer.common.service.OrmTraceService
import org.junit.Test
import java.util.*

class TraceServiceTest {

    protected val service: ITraceService = OrmTraceService()

    protected val time = GregorianCalendar().dayStartTime.time

    protected val myTrace: TraceModel by lazy {
        TraceModel.queryBuilder().findModel<TraceModel>()!!
    }

    protected val serviceId: Int
        get() = myTrace.serviceId

    protected val traceId: Long
        get() = myTrace.id

    @Test
    fun testGetTracesByDuration() {
        val traces = service.getTracesByDuration(serviceId, time, 10, 0, 10000)
        println(traces)
    }

    @Test
    fun testGetTracesByEx() {
        val traces = service.getTracesByEx(serviceId, time, 10)
        println(traces)
    }

    @Test
    fun testGetTraceInfo() {
        val trace = service.getTraceInfo(traceId)
        println(trace)
    }

    /**
     * 准备好span/annotation数据
     */
    @Test
    fun prepareData() {
        clearData()

        val spanId1 = generateId("span")

        // 构建span
        val spans = (1..3).map {i ->
            val s = Span()
            s.name = "span$i"
            s.serviceId = 1
            s.traceId = traceId
            if(i == 1) {
                s.id = spanId1
            }else{
                s.id = generateId("span")
                s.parentId = spanId1
            }
            s
        }
        SpanModel.batchInsert(spans)

        // 构建 annotation
        val keys = listOf("cs", "sr", "ss", "cr")
        val anns = ArrayList<Annotation>()
        var ts = currMillis()
        val (ip, port) = getLocalHostPort()
        for(span in spans){
            for(key in keys){
                val ann = Annotation()
                ann.key = key
                ann.value = ""
                ann.ip = ip
                ann.port = port
                ann.timestamp = ts
                ann.spanId = span.id
                ann.traceId = traceId
                ann.serviceId = 1
                anns.add(ann)

                ts += 1000
            }
        }

        val annEx = Annotation()
        annEx.key = "ex"
        annEx.value = "abc"
        annEx.timestamp = ts
        annEx.ip = ip
        annEx.port = port
        annEx.spanId = spans.last().id
        annEx.traceId = traceId
        anns.add(annEx)

        AnnotationModel.batchInsert(anns)

        println("prepare data")
    }

    @Test
    public fun clearData() {
        // 全部删除
        SpanModel.queryBuilder().delete()
        AnnotationModel.queryBuilder().delete()

        println("clear data")
    }
}
