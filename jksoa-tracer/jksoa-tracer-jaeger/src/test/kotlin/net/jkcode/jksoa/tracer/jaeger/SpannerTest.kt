package net.jkcode.jksoa.tracer.jaeger

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.example.ISimpleService
import org.junit.Test
import java.util.concurrent.CompletableFuture

class SpannerTest {

    @Test
    fun testInitiatorTrace(){
        // 手动加载一下插件
        //JkTracerPlugin().start()

        val span1 = Tracer.current().startInitiatorSpanner(::testInitiatorTrace)

        val req = RpcRequest(ISimpleService::sayHi)
        val span2 = Tracer.current().startClientSpanner(req)
        span2.end()
        span1.end()
    }

    @Test
    fun testServerTrace(){
        val req1 = RpcRequest(ISimpleService::sayHi)
        val span1 = Tracer.current().startServerSpanner(req1)

        val req2 = RpcRequest(ISimpleService::sayHi)
        val span2 = Tracer.current().startClientSpanner(req2)
        span2.end()
        span1.end()
    }

}
