package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.registry.DiscoveryListenerContainer
import net.jkcode.jksoa.rpc.registry.IDiscoveryListener
import net.jkcode.jksoa.rpc.registry.zk.ZkRegistry
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RegistryTests {

    val registry = ZkRegistry()

    val serviceId = "net.jkcode.jksoa.rpc.example.ISimpleService"

    val url = Url("rmi://localhost:8081/$serviceId?weight=1")

    // 服务发现监听器
    private val discoveryListener = object : IDiscoveryListener {

        /**
         * 服务标识，即接口类全名
         */
        override val serviceId: String
            get() = this@RegistryTests.serviceId

        // 处理服务地址新增
        override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
            println("服务[$serviceId]地址新增：" + url)
        }

        // 处理服务地址删除
        override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
            println("服务[$serviceId]地址删除：" + url)
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

    @Test
    fun testDiscoveryListener(){
        val l = DiscoveryListenerContainer(serviceId)
        // 新增
        println("------------ add ------------")
        var urls = listOf(
                Url("jkr://192.168.62.209:9080")
        )
        l.handleServiceUrlsChange(urls)

        // 修改
        println("------------ change1 ------------")
        urls = listOf(
                Url("jkr://192.168.62.209:9080"),
                Url("jkr://192.168.62.210:9080")
        )
        l.handleServiceUrlsChange(urls)

        // 修改
        println("------------ change2 ------------")
        urls = listOf(
                Url("jkr://192.168.62.209:9080?weight=10")
        )
        l.handleServiceUrlsChange(urls)

        // 删除
        println("------------ remove ------------")
        urls = emptyList()
        l.handleServiceUrlsChange(urls)
    }


}