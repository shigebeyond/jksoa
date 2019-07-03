package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.example.ISimpleService
import net.jkcode.jksoa.server.IRpcServer
import net.jkcode.jksoa.tracer.agent.plugin.RpcClientPlugin
import org.junit.Test
import java.util.concurrent.CompletableFuture

@TraceableService
class TracerTest {

    @Test
    fun testServer(){
        // 启动server
        IRpcServer.instance("netty").start()
    }

    @Test
    fun testInitiatorTrace(){
        // 手动加载一下插件
        RpcClientPlugin().start()

        val span1 = Tracer.current().startInitiatorSpanSpanner(::testInitiatorTrace)

        val req = RpcRequest(ISimpleService::echo)
        val span2 = Tracer.current().startClientSpanSpanner(req)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

    @Test
    fun testServerTrace(){
        val req1 = RpcRequest(ISimpleService::echo)
        val span1 = Tracer.current().startServerSpanSpanner(req1)

        val req2 = RpcRequest(ISimpleService::echo)
        val span2 = Tracer.current().startClientSpanSpanner(req2)
        val f2 = span2.end()

        val f1 = span1.end()

        CompletableFuture.allOf(f1, f2).get()
    }

}
