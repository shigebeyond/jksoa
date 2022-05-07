package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.getIntranetHost
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jksoa.rpc.server.IRpcServer
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import net.jkcode.jksoa.rpc.server.protocol.jkr.JkrRpcServer
import net.jkcode.jksoa.rpc.server.protocol.rmi.RmiRpcServer
import org.junit.Test
import kotlin.reflect.jvm.javaMethod

/**
 * 测试server
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RpcServerTests {

    @Test
    fun testServer(){
        IRpcServer.instance("jkr").start()
    }

    @Test
    fun testUrl(){
        val config = Config.instance("rpc-server", "yaml")
        val url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!, "net.jkcode.jksoa.rpc.example.ISimpleService", config.getMap("parameters", emptyMap<String, Any?>())!!)
        println(url)
    }

    @Test
    fun testRmiRpcServer(){
        RmiRpcServer().start()
        println("启动服务")
    }

    @Test
    fun testJkrRpcServer(){
        JkrRpcServer().start()
        println("启动服务")
    }

}