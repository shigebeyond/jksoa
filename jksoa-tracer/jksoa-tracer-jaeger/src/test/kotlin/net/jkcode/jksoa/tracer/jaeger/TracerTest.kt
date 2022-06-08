package net.jkcode.jksoa.tracer.jaeger

import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService
import org.junit.Test

class TracerTest {

    @Test
    fun testTrace(){
        val span = Tracer.current().startInitiatorSpanner(::testTrace)

        val service = Referer.getRefer<ISimpleService>()
        val ret = service.hostname()
        println("调用服务[ISimpleService.hostname()]结果： $ret")

        span.end()
    }

}
