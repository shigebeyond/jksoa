package net.jkcode.jksoa.tracer.collector

import net.jkcode.jksoa.rpc.server.IRpcServer
import org.junit.Test

class CollectorTest {

    @Test
    fun testServer(){
        // 启动server
        IRpcServer.instance("jkr").start()
    }

}
