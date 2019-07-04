package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.client.referer.Referer
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
    fun testTrace(){
        val span = Tracer.current().startInitiatorSpanner(::testTrace)

        val service = Referer.getRefer<ISimpleService>()
        val pong = service.ping()
        println("调用服务[ISimpleService.ping()]结果： $pong")

        span.end().get()
    }

}
