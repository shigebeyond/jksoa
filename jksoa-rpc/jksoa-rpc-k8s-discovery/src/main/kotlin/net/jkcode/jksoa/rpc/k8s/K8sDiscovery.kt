package net.jkcode.jksoa.rpc.k8s

import net.jkcode.jksoa.common.k8sLogger
import net.jkcode.jksoa.rpc.client.k8s.K8sUtil
import net.jkcode.jkutil.collection.DataFrame
import net.jkcode.jkutil.common.*
import java.util.concurrent.TimeUnit

/**
 * k8s模式下的服务发现者
 *   负责在 docker manager node中定时运行 docker service ls 命令来查询k8s服务的节点数，并广播服务节点数消息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2023-7-12 11:22 AM
 */
object K8sDiscovery {

    /**
     * 查询k8s服务的节点数的命令
     */
    private val queryCmd = "kubectl get deploy -A"

    /**
     * 上一次查询结果的文本
     */
    private var lastQueryResult: Map<String, Int> = emptyMap()

    /**
     * 通过 docker service ls 命令来查询k8s服务的节点数
     * @return Map<k8s服务名, 节点数>
     */
    private fun queryK8sServiceReplicas(): HashMap<String, Int>? {
        /* 1 执行命令，结果如下
        NAMESPACE       NAME                       READY   UP-TO-DATE   AVAILABLE   AGE
        default         demo                       2/2     2            2           27h
         */
        val output = execCommand(queryCmd)
        if(output.isNullOrBlank())
            return null

        // 2 转df
        val df = DataFrame.fromString(output)!!

        // 3 解析副本数
        val services = HashMap<String, Int>()
        df.forEach { row ->
            val name = row["NAME"]!!
            // 可用副本数/期望副本数，在滚动更新的过程中有可能：可用副本数 > 期望副本数，因此要取最小值
            val (readyReplicas, desiredReplicas) = row["READY"]!!.split('/')
            val replicas = minOf(readyReplicas.toInt(), desiredReplicas.toInt())
            services[name] = replicas
        }

        return services
    }

    /**
     * 广播服务节点数消息
     *    1 要发全量数据，因为client可能刚开始监听
     *    2 数据没变化也要发，因为client可能刚开始监听，但可以随机的发，以便减少重复消息发送
     * @param data
     */
    private fun sendMq(data: HashMap<String, Int>?) {
        // 1 无服务: 可能不是管理节点
        if (data.isNullOrEmpty()) {
            k8sLogger.error("查询k8s服务的节点数为空, 请检查当前主机是否docker管理节点, 并执行命令查看是否有服务: {}", queryCmd)
            return
        }

        // 2 有服务：广播消息
        var sending = true
        if (lastQueryResult == data) { // 不变
            sending = randomInt(10) < 7 // 70%几率随机发
            val sendMsg = if(sending) "随机发" else "不发"
            k8sLogger.info("查询k8s服务的节点数无变化+{}: {}", sendMsg, data)
        }else{ // 有变
            k8sLogger.info("查询k8s服务的节点数有变化: {}", data)
            lastQueryResult = data
        }

        // 广播消息
        if(sending)
            K8sUtil.mqMgr.sendMq(K8sUtil.topic, data)
    }

    /**
     * 启动定时查询k8s服务的节点数
     * @param timerSeconds
     */
    public fun start(timerSeconds: Long = 10){
        CommonSecondTimer.newPeriodic({
            // 查询k8s服务的节点数
            val data = queryK8sServiceReplicas()
            // 广播消息
            sendMq(data)
        }, timerSeconds, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start()
    }
}