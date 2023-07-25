package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.client.k8s.K8sConnectionHub
import net.jkcode.jksoa.rpc.client.k8s.K8sConnections
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.dateFormat
import net.jkcode.jkutil.common.execCommand
import net.jkcode.jkutil.common.groupCount
import org.junit.Test
import java.util.*

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
        Thread.sleep(5000)
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

    /**
     * 一直启动 K8sConnectionHub 监听副本变化的mq，每隔5s打印下连接
     */
    @Test
    fun testDiscoveryListenerAndWaitReplicaMq(){
        while (true){
            printConns(Date().toString())
            Thread.sleep(10000)
        }
    }

    /**
     * 2个容器副本，kill掉一个，观察kill前后的连接，等10秒，因为kill后会重启容器，观察重启后的连接变化
     */
    @Test
    fun testSwarmRebalanceConns(){
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
        printConns("下线后重连", true)

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

        printConns("均衡后")
    }

    /**
     * 2个pod副本，kill掉一个，观察kill前后的连接，等10秒，因为kill后会重启pod，观察重启后的连接变化
     */
    @Test
    fun testK8sRebalanceConns(){
        // 建立连接 -- client连到2台server
        println("---------- 建立2台server的连接 ---------")
        var url = "$serverAddr?replicas=2"
        K8sConnectionHub.handleServiceUrlAdd(Url(url), emptyList())
        printConns("初始")

        /*
        // 延迟创建连接(未建立连接)时, 测试均衡
        K8sConnectionHub.rebalanceConns()
        printConns("均衡后")
        */

        println("---------- 操作下线1台server ---------")
        // 要营造测试场景: 某台worker server下线 -- 1台server，副本数应该减少，但没有通知client
        // 场景一：2台物理机：下线一台
        //val ret = execCommand("kubectl cordon mac") // kubectl uncordon mac
        // println(ret)

        // 场景二：1台物理机，2个pod：停掉一个pod
        var podNames = execCommand("kubectl get pod -o custom-columns=:.metadata.name").trim().split("\n")
        println("有pod名: " + podNames + ", 关掉pod: " + podNames.first())
        val ret = execCommand("kubectl delete pod " + podNames.first())
        println(ret)

        printConns("下线后")
        printConns("下线后重连", true)

        println("---------- 下线后立即均衡连接: 全部连上剩下的一台server ---------")
        // 均衡连接: server1
        K8sConnectionHub.rebalanceConns()
        printConns("均衡后")

        Thread.sleep(10000)

        println("---------- 等新server起来后的均衡连接：连上2台server ---------")
        podNames = execCommand("kubectl get pod -o custom-columns=:.metadata.name").trim().split("\n")
        println("有pod名: " + podNames)
        // 均衡连接
        K8sConnectionHub.rebalanceConns()
        printConns("均衡后")
    }

    /**
     * 检查连接
     */
    private fun printConns(tag: String, reconnect: Boolean = false) {
        println("---------- $tag-检查连接的serverId ---------")
        val conns = K8sConnectionHub.getOrCreateConn(serverAddr)!!
        var i = 0
        for (conn in conns) {
            println("第 $i 个连接, 有效=" + conn.isValid()  +", serverId=" + conn.getServerId(reconnect /* 强制重连 */))
            i++
        }
        val serverNums = conns.groupCount() {
            it.getServerId() ?: ""
        }
        println("按server分组连接: " + serverNums)
    }


}