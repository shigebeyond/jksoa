package net.jkcode.jksoa.tracer.jaeger

import com.alibaba.fastjson.JSONObject
import io.jaegertracing.internal.JaegerSpan
import io.jaegertracing.spi.Reporter
import lombok.extern.slf4j.Slf4j
import net.jkcode.jksoa.tracer.common.tracerLogger

@Slf4j
class TraceReporter : Reporter {
    override fun report(span: JaegerSpan) {
        tracerLogger.info(span.toString())

        val jsonObject = JSONObject()
        jsonObject["traceID"] =  span.context().traceId
        jsonObject["spanID"] =  span.context().spanId

        val parentId = span.context().parentId
        jsonObject["parentSpanID"] =  java.lang.Long.toHexString(parentId)
        jsonObject["startTime"] =  span.start
        jsonObject["duration"] =  span.duration
        jsonObject["operationName"] =  span.operationName
        val tags = span.tags
        for ((key, value) in tags) {
            jsonObject["tags." + key] = value.toString()
        }
        tracerLogger.info(jsonObject.toString())
    }

    override fun close() {}
}