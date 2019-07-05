package net.jkcode.jksoa.tracer.web

import net.jkcode.jkmvc.server.JettyServer
import org.junit.Test

class WebTest{

    // right: 直接运行 JettyServerLauncher 类, 并设置 module 为 jksoa-tracer_main
    @Test
    fun testJettyServer() {
        // wrong: 无法加载 jksoa-tracer/src/main/webapp
        JettyServer().start()
    }

}