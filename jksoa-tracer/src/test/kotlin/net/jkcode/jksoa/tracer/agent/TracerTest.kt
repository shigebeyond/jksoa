package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.example.ISimpleService
import net.jkcode.jksoa.server.IRpcServer
import org.junit.Test

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
