package net.jkcode.jksoa.rpc.k8s

import net.jkcode.jksoa.rpc.client.k8s.K8sConnectionHub
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.common.groupCount

object K8sClientTest {

    val serverAddr = "jkr://rpcserver.default:9080"

    @JvmStatic
    fun main(args: Array<String>) {
        while(true) {
            printConns()
            val service = Referer.getRefer<ISimpleService>()
            val ret = service.podInfo()
            println("调用服务[ISimpleService.podInfo()]结果： $ret")
            Thread.sleep(5000)
        }
    }

    /**
     * 检查连接
     */
    private fun printConns(reconnect: Boolean = false) {
        println("---------- 检查连接的serverId ---------")
        val conns = K8sConnectionHub.getOrCreateConn(serverAddr)!!
        // 查看每个连接的serverid
        for (i in 0 until conns.size) {
            val conn = conns[i]
            val serverId = conn.getServerId(reconnect) // reconnect控制重连
            println("第 $i 个连接, 有效=" + conn.isValid()  +", serverId=" + serverId)
        }
        // 统计每个server的连接数，看看server连接是否均衡
        val serverNums = conns.groupCount() {
            it.getServerId() ?: ""
        }
        println("按server统计连接数: " + serverNums)
    }

}