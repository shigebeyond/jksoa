package net.jkcode.jksoa.tracer.jaeger

import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.common.Config
import org.junit.Test

class TracerTest {

    @Test
    fun testConfig(){
        val config = Config.instance("jaeger", "properties")
        println(config.props)
    }

    @Test
    fun testTrace(){
        val span = Tracer.current().startInitiatorSpanner(::testTrace)

        val service = Referer.getRefer<ISimpleService>()
        val ret = service.sayHi()
        println("调用服务[ISimpleService.sayHi()]结果： $ret")

        span.end()
    }

}
