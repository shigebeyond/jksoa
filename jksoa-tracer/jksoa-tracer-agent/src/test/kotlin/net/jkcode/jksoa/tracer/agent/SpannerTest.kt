package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jksoa.tracer.agent.plugin.RpcClientTracerPlugin
import org.junit.Test
import java.util.concurrent.CompletableFuture

@TraceableService
class SpannerTest {

    @Test
    fun testInitiatorTrace(){
        // 手动加载一下插件
        RpcClientTracerPlugin().start()

        val span1 = Tracer.current().startInitiatorSpanner(::testInitiatorTrace)

        val req = RpcRequest(ISimpleService::sayHi)
        val span2 = Tracer.current().startClientSpanner(req)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

    @Test
    fun testServerTrace(){
        val req1 = RpcRequest(ISimpleService::sayHi)
        val span1 = Tracer.current().startServerSpanner(req1)

        val req2 = RpcRequest(ISimpleService::sayHi)
        val span2 = Tracer.current().startClientSpanner(req2)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

}
