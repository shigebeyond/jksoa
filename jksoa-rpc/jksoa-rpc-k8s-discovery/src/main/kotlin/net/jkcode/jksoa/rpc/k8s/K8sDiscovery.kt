package net.jkcode.jksoa.rpc.k8s

import net.jkcode.jksoa.common.k8sLogger
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.client.k8s.K8sUtil
import net.jkcode.jkutil.common.*
import org.yaml.snakeyaml.Yaml
import java.util.concurrent.TimeUnit

/**
 * k8s模式下的服务发现者
 *   负责在 k8s master node中定时运行 kubectl get deploy -A 命令来查询k8s应用的节点数，并广播应用节点数消息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2023-7-12 11:22 AM
 */
object K8sDiscovery {

    /**
     * 配置
     */
    private val config: Config = Config.instance("k8s-discovery", "yaml")

    /**
     * k8s集群中统一的rpc容器端口
     */
    private val rpcContainerPort: Int = config["rpcContainerPort"]!!

    /**
     * 服务发现关注哪些k8s命名空间
     */
    private var k8sNs: String? = IConnectionHub.config["k8sns"]

    /**
     * 查询k8s部署的节点数的命令
     */
    private val deploymentCmd = if(k8sNs == null)
                                    "kubectl get deploy -A -o yaml" //  如果不指定命名空间则取全部命名空间
                                else
                                    "kubectl get deploy -n $k8sNs -o yaml" // 过滤命名空间

    /**
     * 上一次查询结果的文本
     */
    private var lastQueryResult: Map<String, Int> = emptyMap()

    /**
     * 通过 kubectl get deploy -A 命令来查询rpc应用的节点数
     * @return Map<k8s应用域名, 节点数>
     */
    private fun queryK8sAppReplicas(): HashMap<String, Int>? {
        // 执行命令, 结果为yaml
        val yaml = execCommand(deploymentCmd)
        if(yaml.isNullOrBlank())
            return null

        // 解析yaml
        val yamlObj = Yaml().loadAs(yaml, HashMap::class.java)
        val items = yamlObj["items"] as List<Map<String, Map<String, Any?>>>

        // 从yaml中收集每个应用的副本数
        val app2replicas = HashMap<String, Int>()
        for (item in items){
            val appAndReplicas = parseAppAndReplicas(item)
            if (appAndReplicas != null){
                val (app, replicas) = appAndReplicas
                app2replicas[app] = replicas
            }
        }

        return app2replicas
    }

    /**
     * 解析rpc应用名+副本数
     */
    private fun parseAppAndReplicas(item: Map<String, Map<String, Any?>>): Pair<String, Int>? {
        // 1 检查app标签, 并获得app名
        val meta = item["metadata"]!!
        val labels = meta["labels"] as Map<String, Any?>
        var app = labels["app"] as String? // app名
        if (app == null) // 必须要有app标签, 否则不认为是rpc应用
            return null
        app = app + '.' + meta["namespace"] // k8s的应用域名 = 应用名.命名空间

        // 2 检查是否有rpc容器端口: spec.template.spec.containers[].ports[].containerPort
        // 获得容器
        val containers = PropertyUtil.getPath(item, "spec.template.spec.containers") as List<Map<String, Any?>>
        // 检查是否有rpc容器端口
        val hasRpcContainerPort = containers.any { container ->
            val ports = container["ports"] as List<Map<String, Any?>>? // 获得容器端口
            ports?.any { p ->
                rpcContainerPort == p["containerPort"] // 必须匹配rpc容器端口, 否则不认为是rpc应用
            } ?: false
        }
        if(!hasRpcContainerPort)
            return null

        // 3 获得副本数
        // 可用副本数/期望副本数，在滚动更新的过程中有可能：可用副本数 > 期望副本数，因此要取最小值
        val status = item["status"] as Map<String, Int>
        val readyReplicas = status["readyReplicas"]!! // 可用副本数
        val desiredReplicas = status["replicas"]!! // 期望副本数
        // 扩容中或扩容结束: 取最小值
        var replicas = minOf(readyReplicas, desiredReplicas)
        // 缩容中: 还是返回旧的副本数, 等下次缩容结束后再返回新的副本数
        if (readyReplicas > desiredReplicas) {
            replicas = lastQueryResult[app] ?: replicas
        }
        return Pair(app, replicas)
    }

    /**
     * 广播应用节点数消息
     *    1 要发全量数据，因为client可能刚开始监听
     *    2 数据没变化也要发，因为client可能刚开始监听，但可以随机的发，以便减少重复消息发送
     * @param data
     */
    private fun sendMq(data: HashMap<String, Int>?) {
        // 1 无服务: 可能不是管理节点
        if (data.isNullOrEmpty()) {
            k8sLogger.error("查询k8s应用的节点数为空, 请检查当前主机是否docker管理节点, 并执行命令查看是否有服务: {}", deploymentCmd)
            return
        }

        // 2 有服务：广播消息
        var sending = true
        if (lastQueryResult == data) { // 不变
            sending = randomInt(10) < 7 // 70%几率随机发
            val sendMsg = if(sending) "随机发" else "不发"
            k8sLogger.info("查询k8s应用的节点数无变化+{}: {}", sendMsg, data)
        }else{ // 有变
            k8sLogger.info("查询k8s应用的节点数有变化: {}", data)
            lastQueryResult = data
        }

        // 广播消息
        if(sending)
            K8sUtil.mqMgr.sendMq(K8sUtil.topic, data)
    }

    /**
     * 启动定时查询k8s应用的节点数
     */
    public fun start(){
        CommonSecondTimer.newPeriodic({
            // 查询k8s应用的节点数
            val data = queryK8sAppReplicas()
            // 广播消息
            sendMq(data)
        }, config["discoverTimerSeconds"]!!, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start()
    }
}