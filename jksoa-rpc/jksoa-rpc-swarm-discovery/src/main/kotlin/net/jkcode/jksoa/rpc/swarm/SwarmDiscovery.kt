package net.jkcode.jksoa.rpc.swarm

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.swarm.SwarmUtil
import net.jkcode.jkutil.common.*
import java.util.concurrent.TimeUnit

/**
 * docker swarm模式下的服务发现者
 *   负责在 docker manager node中定时运行 docker service ls 命令来查询swarm服务的节点数，并广播服务节点数消息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object SwarmDiscovery {

    /**
     * 查询swarm服务的节点数的命令
     */
    private val queryCmd = "docker service ls --format {{.Name}}:{{.Replicas}}"

    /**
     * 上一次查询结果的文本
     */
    private var lastQueryResult: Map<String, Int> = emptyMap()

    /**
     * 通过 docker service ls 命令来查询swarm服务的节点数
     * @return Map<swarm服务名, 节点数>
     */
    private fun querySwarmServiceReplicas(): HashMap<String, Int>? {
        // 1 exec comd
        // https://docs.docker.com/engine/reference/commandline/service_ls/
        val pro: Process = Runtime.getRuntime().exec(queryCmd)
        val status = pro.waitFor()
        if (status != 0)
            throw RpcClientException("Failed to call command: $queryCmd")

        // output eg. tcp_tcpserver:1/2
        val text = pro.output()
        if(text.isNullOrBlank())
            return null

        // 2 parse service and replicas
        val services = HashMap<String, Int>()
        for (line in text.split("\n")) {
            if(line.isEmpty())
                break
            // tcp_tcpserver:1/2 = 服务名:实际副本数/需要副本数
            val (service, actualReplicas, desiredReplicas) = line.split(':', '/')
            services[service] = actualReplicas.toInt()
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
            clientLogger.error("查询swarm服务的节点数为空, 请检查当前主机是否docker管理节点, 并执行命令查看是否有服务: {}", queryCmd)
            return
        }

        // 2 有服务：广播消息
        var sending = true
        if (lastQueryResult == data) { // 不变
            sending = randomInt(10) < 7 // 70%几率随机发
            val sendMsg = if(sending) "随机发" else "不发"
            clientLogger.info("查询swarm服务的节点数无变化+{}: {}", sendMsg, data)
        }else{ // 有变
            clientLogger.info("查询swarm服务的节点数有变化: {}", data)
            lastQueryResult = data
        }

        // 广播消息
        if(sending)
            SwarmUtil.mqMgr.sendMq(SwarmUtil.topic, data)
    }

    /**
     * 启动定时查询swarm服务的节点数
     * @param timerSeconds
     */
    public fun start(timerSeconds: Long = 10){
        CommonSecondTimer.newPeriodic({
            // 查询swarm服务的节点数
            val data = querySwarmServiceReplicas()
            // 广播消息
            sendMq(data)
        }, timerSeconds, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start()
    }
}