package net.jkcode.jksoa.rpc.k8s

import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService

object K8sClientTest {

    @JvmStatic
    fun main(args: Array<String>) {
        while(true) {
            val service = Referer.getRefer<ISimpleService>()
            val ret = service.podInfo()
            println("调用服务[ISimpleService.podInfo()]结果： $ret")
            Thread.sleep(5000)
        }
    }

}