package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.client.k8s.K8sConnectionHub
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.execCommand
import org.junit.Test

class K8sConnectionTests {

    /**
     * 客户端配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    init {
        // 预先创建连接
        config["lazyConnect"] = false
    }

    val serverAddr = "jkr://rpcserver.default:9080"

    @Test
    fun testDiscoveryListener(){
        // 新增
        println("------------ add ------------")
        var url = serverAddr
        K8sConnectionHub.handleServiceUrlAdd(Url(url), emptyList())

        // 修改
        println("------------ change1 ------------")
        K8sConnectionHub.handleParametersChange(Url(url + "?replicas=2"))

        // 修改
        println("------------ change2 ------------")
        K8sConnectionHub.handleParametersChange(Url(url + "?replicas=1"))

        // 删除
        println("------------ remove ------------")
        K8sConnectionHub.handleServiceUrlRemove(Url(url), emptyList())
    }

    @Test
    fun testDiscoveryListener2(){
        val server = "rpcserver.default"

        // 新增
        println("------------ add ------------")
        K8sConnectionHub.handleK8sServiceReplicasChange(mutableMapOf(server to 1))

        // 修改
        println("------------ change1 ------------")
        K8sConnectionHub.handleK8sServiceReplicasChange(mutableMapOf(server to 2))

        // 修改
        println("------------ change2 ------------")
        K8sConnectionHub.handleK8sServiceReplicasChange(mutableMapOf(server to 1))

        // 删除
        println("------------ remove ------------")
        K8sConnectionHub.handleK8sServiceReplicasChange(mutableMapOf())
    }

    @Test
    fun testRebalanceConns(){
        // 建立连接 -- client连到2台server
        println("---------- 建立2台server的连接 ---------")
        var url = "$serverAddr?replicas=2"
        K8sConnectionHub.handleServiceUrlAdd(Url(url), emptyList())
        printConns("初始")

        println("---------- 操作下线1台server ---------")
        // 要营造测试场景: 某台worker server下线 -- 1台server，副本数应该减少，但没有通知client
        // 场景一：2台物理机：下线一台
        //val ret = execCommand("docker node update --availability drain shi-WK") // docker node update --availability active shi-WK
        // println(ret)

        // 场景二：1台物理机，2个容器：停掉一个容器
        var containIds = execCommand("docker ps --format {{.ID}}").trim().split("\n")
        println("有容器id: " + containIds + ", 关掉容器: " + containIds.first())
        val ret = execCommand("docker stop " + containIds.first())
        println(ret)

        printConns("下线后")
        printConns("下线后重连")

        println("---------- 下线后立即均衡连接: 全部连上剩下的一台server ---------")
        // 均衡连接: server1
        K8sConnectionHub.rebalanceConns()

        printConns("均衡后")

        Thread.sleep(10000)

        println("---------- 等新server起来后的均衡连接：连上2台server ---------")
        containIds = execCommand("docker ps --format {{.ID}}").trim().split("\n")
        println("有容器id: " + containIds)
        // 均衡连接
        K8sConnectionHub.rebalanceConns()

        println("---------- 均衡后发rpc ---------")
        printConns("均衡后")
    }

    /**
     * 检查连接
     */
    private fun printConns(tag: String) {
        println("---------- $tag-检查连接的serverId ---------")
        val conns = K8sConnectionHub.getOrCreateConn(serverAddr)!!
        var i = 0
        for (conn in conns) {
            println("第 $i 个连接, 有效=" + conn.isValid()  +", serverId=" + conn.serverId)
            i++
        }
    }


}