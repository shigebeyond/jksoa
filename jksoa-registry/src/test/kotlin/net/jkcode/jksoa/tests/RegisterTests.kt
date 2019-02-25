package net.jkcode.jksoa.tests

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.registry.IDiscoveryListener
import net.jkcode.jksoa.registry.zk.ZkRegistry
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RegisterTests {

    val registry = ZkRegistry

    val serviceId = "net.jkcode.jksoa.example.IEchoService"

    val url = Url("rmi://localhost:8081/$serviceId?weight=1")

    // 服务发现监听器
    private val discoveryListener = object : IDiscoveryListener {
        // 处理服务地址变化
        override fun handleServiceUrlsChange(serviceId: String, urls: List<Url>) {
            println("服务[$serviceId]地址变化：" + urls)
        }

        // 处理服务参数变化
        override fun handleParametersChange(url: Url) {
            println("服务参数变化：" + url)
        }
    }

    @Test
    fun testRegister(){
        println("注册服务：$url")
        registry.register(url)
    }

    @Test
    fun testUnregister(){
        println("注销服务：$url")
        registry.unregister(url)
    }

    @Test
    fun testSubscribe(){
        println("订阅服务：$serviceId")
        registry.subscribe(serviceId, discoveryListener)
    }

    @Test
    fun testUnsubscribe(){
        println("退订服务：$serviceId")
        registry.unsubscribe(serviceId, discoveryListener)
    }


}