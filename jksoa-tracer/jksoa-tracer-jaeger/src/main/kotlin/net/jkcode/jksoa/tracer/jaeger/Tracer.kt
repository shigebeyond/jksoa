package net.jkcode.jksoa.tracer.jaeger

import io.jaegertracing.internal.JaegerTracer
import io.jaegertracing.internal.samplers.ConstSampler
import io.jaegertracing.internal.samplers.ProbabilisticSampler
import io.jaegertracing.spi.Reporter
import io.jaegertracing.spi.Sampler
import io.opentracing.Span
import io.opentracing.SpanContext
import io.opentracing.noop.NoopSpan
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMap
import io.opentracing.propagation.TextMapAdapter
import io.opentracing.tag.Tags
import io.opentracing.util.GlobalTracer
import net.jkcode.jkmvc.orm.serialize.toJson
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jkutil.common.JkApp
import net.jkcode.jkutil.ttl.AllRequestScopedTransferableThreadLocal
import net.jkcode.jkutil.ttl.SttlCurrentHolder

/**
 * 系统跟踪类
 * 都需要创建trace才能操作其他api
 * 用#号前缀来标识发起人的service
 *
 * 在client
 * 1. 第一次创建trace, 即可创建 rootspan, 其parentid为null, 记录为后面span的parentspan -- http处理/定时任务处理
 * 2. 后面创建span, 都以rootspan作为parent -- rpc调用
 *
 * 在rpc server
 * 1 收到请求时, 第一次创建trace, 需要从请求中获得parentSpan
 *
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-29 6:19 PM
 */
class Tracer protected constructor() : ITracer() {

    companion object : SttlCurrentHolder<Tracer>(AllRequestScopedTransferableThreadLocal { Tracer() }) { // 所有请求域的可传递的 ThreadLocal

        /**
         * 注册全局tracer
         */
        init {
            clientLogger.info("initialized the global tracer")
            val reporter: Reporter = TraceReporter()
            val samplingRate: Double = 0.1
            var sampler: Sampler = if (samplingRate >= 0.0 && samplingRate <= 1.0)
                ProbabilisticSampler(samplingRate)
            else
                ConstSampler(true)

            val tracer = JaegerTracer.Builder(JkApp.name)
                    .withReporter(reporter)
                    .withSampler(sampler)
                    .build()
            GlobalTracer.registerIfAbsent(tracer)
        }
    }

    /**
     * 新建发起人的span
     *
     * @param serviceName
     * @param name
     * @param params
     * @return
     */
    public override fun startInitiatorSpanner(serviceName: String, name: String, params: Map<String, *>): Span {
        // 创建span
        val apiName = serviceName + ":" + name
        val span = GlobalTracer.get().buildSpan(apiName).start()
        span.setTag("arguments", params.toJson()) // rpc的参数

        // 发起人, 作为当前线程的后续span的父span
        parentSpan = span

        Tags.SPAN_KIND[span] = Tags.SPAN_KIND_CLIENT
        return span
    }

    /**
     * 新建客户端的span
     *
     * @param req
     * @return
     */
    public override fun startClientSpanner(req: IRpcRequest): Span {
        // 以当前发起人/服务端的span为 父span
        val parentSpanContext = parentSpan?.context()

        // 构建span
        val span = buildRpcSpan(req, parentSpanContext)

        // 向下游传递的参数
        GlobalTracer.get().inject(span.context(), Format.Builtin.TEXT_MAP, object : TextMap {
            override fun put(key: String, value: String) {
                req.putAttachment(key, value)
            }

            override fun iterator(): MutableIterator<Map.Entry<String, String>> {
                throw UnsupportedOperationException("TextMapInjectAdapter should only be used with Tracer.inject()")
            }
        })

        Tags.SPAN_KIND[span] = Tags.SPAN_KIND_CLIENT
        return span
    }

    /**
     * 新建服务端的span
     *
     * @param req
     * @return
     */
    public override fun startServerSpanner(req: IRpcRequest): Span {
        // 根据请求的附加参数来确定父span上下文
        val parentSpanMap = TextMapAdapter(req.attachments as MutableMap<String, String>) // todo: 可能为null
        val parentSpanContext = GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, parentSpanMap)
        if (parentSpanContext == null)
            return NoopSpan.INSTANCE

        // 构建span
        val span = buildRpcSpan(req, parentSpanContext)

        // server端receive, 作为当前线程的后续span的父span
        this.parentSpan = span

        Tags.SPAN_KIND[span] = Tags.SPAN_KIND_SERVER
        return span
    }

    /**
     * 构建span
     */
    public fun buildRpcSpan(req: IRpcRequest, parentSpanContext: SpanContext?): Span {
        // api名
        val apiName = req.serviceId + ":" + req.methodSignature
        val spanBuilder = GlobalTracer.get().buildSpan(apiName)
        // 父span
        if (parentSpanContext != null) {
            spanBuilder.asChildOf(parentSpanContext)
        }
        val span = spanBuilder.start()
        // rpc的参数
        span.setTag("arguments", req.args.toJson())
        return span
    }

}