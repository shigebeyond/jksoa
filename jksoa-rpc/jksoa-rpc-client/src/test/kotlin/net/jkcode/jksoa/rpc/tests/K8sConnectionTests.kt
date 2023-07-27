package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.client.k8s.K8sConnection
import net.jkcode.jksoa.rpc.client.k8s.K8sConnectionHub
import net.jkcode.jksoa.rpc.client.k8s.K8sConnections
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.collection.DataFrame
import net.jkcode.jkutil.common.*
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

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
        K8sConnectionHub.handleK8sServiceReplicasChange(mutableMapOf<String, Int>())
    }

    /**
     * 一直启动 K8sConnectionHub 监听副本变化的mq，每隔5s打印下连接
     *   要先启动 K8sDiscovery 进程，来发副本变化的mq
     */
    @Test
    fun testDiscoveryListenerAndWaitReplicaMq(){
        while (true){
            printConns(Date().toString())
            // 挑一个连接发请求，及时发现kill掉的pod连接
            pickConnRpc()
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
        //val output = execCommand("docker node update --availability drain shi-WK") // docker node update --availability active shi-WK
        // println(output)

        // 场景二：1台物理机，2个容器：停掉一个容器
        var containIds = execCommand("docker ps --format {{.ID}}").trim().split("\n")
        println("有容器id: " + containIds + ", 关掉容器: " + containIds.first())
        val output = execCommand("docker stop " + containIds.first())
        println(output)

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
        //val output = execCommand("kubectl cordon mac") // kubectl uncordon mac
        // println(output)

        // 场景二：1台物理机，2个pod：停掉一个pod
        var podNames = execCommand("kubectl get pod -o custom-columns=:.metadata.name").trim().split("\n")
        println("有pod名: " + podNames + ", 关掉pod: " + podNames.first())
        val output = execCommand("kubectl delete pod " + podNames.first())
        println(output)

        printConns("下线后")
        checkPodNotExistConns()
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

    /**
     * 挑一个连接发请求，及时发现kill掉的pod连接
     *   先挑pod不存在的连接，如果没有，再随机挑一个
     */
    private fun pickConnRpc() {
        val conns = K8sConnectionHub.getOrCreateConn(serverAddr)!!
        // 先挑pod不存在的连接，如果没有，再随机挑一个
        val invalidIdx = checkPodNotExistConns()
        var i:Int
        if (invalidIdx.isNotEmpty()) //pod不存在的连接
            i = invalidIdx.first()
        else // 随机的连接
            i = randomInt(conns.size)
        println("---------- 测试第 $i 个连接的rpc ---------")
        val conn = conns[i]
        var hasException = false
        try {
            val req = RpcRequest(ISimpleService::hostname)
            conn.send(req, 1000).get()
        } catch (e: Exception) {
            e.printStackTrace()
            hasException = true
        }
        val serverId = conn.getServerId(hasException) // 异常重连
        println("第 $i 个连接, 有效=" + conn.isValid() + ", serverId=" + serverId)
    }

    /**
     * 通过k8s命令检查pod不存在的连接
     */
    private fun checkPodNotExistConns(): ArrayList<Int> {
        // 获得pod名
        val output = execCommand("kubectl get pod")
        val df = DataFrame.fromString(output)!!
        val pods = ArrayList<String>()
        df.forEach {
            if(it["STATUS"] == "Running")
                pods.add(it["NAME"]!!)
        }
        println("有效的pod: " + pods)
        // 对比连接的serverid vs pod名
        val inValidConns = ArrayList<Int>()
        val conns = K8sConnectionHub.getOrCreateConn(serverAddr)!!
        for (i in 0 until conns.size) {
            val conn = conns[i]
            val serverId = conn.getServerId()
            if(serverId != null && serverId !in pods){
                println("!!!! 第 $i 个连接 连到失效的pod: " + serverId)
                inValidConns.add(i)
            }
        }
        return inValidConns
    }

}