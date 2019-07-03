package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.example.SimpleService
import net.jkcode.jksoa.server.IRpcServer
import org.junit.Test
import java.util.concurrent.CompletableFuture

class TracerTest {

    @Test
    fun testServer(){
        // 启动server
        IRpcServer.instance("netty").start()
    }

    @Test
    fun testInitiatorTrace(){
        val span1 = Tracer.current().startInitiatorSpanSpanner(::testInitiatorTrace)

        val req = RpcRequest(SimpleService::echo)
        val span2 = Tracer.current().startClientSpanSpanner(req)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

    @Test
    fun testServerTrace(){
        val req1 = RpcRequest(SimpleService::echo)
        val span1 = Tracer.current().startServerSpanSpanner(req1)

        val req2 = RpcRequest(SimpleService::echo)
        val span2 = Tracer.current().startClientSpanSpanner(req2)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

}
