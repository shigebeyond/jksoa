package net.jkcode.jksoa.tracer.agent

import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService
import org.junit.Test

@TraceableService
class TracerTest {

    @Test
    fun testTrace(){
        val span = Tracer.current().startInitiatorSpanner(::testTrace)

        val service = Referer.getRefer<ISimpleService>()
        val pong = service.ping()
        println("调用服务[ISimpleService.ping()]结果： $pong")

        span.end().get()
    }

}
