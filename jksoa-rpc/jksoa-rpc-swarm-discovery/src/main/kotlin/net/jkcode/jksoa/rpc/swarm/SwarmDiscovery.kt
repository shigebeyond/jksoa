package net.jkcode.jksoa.rpc.swarm

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.swarm.SwarmUtil
import net.jkcode.jkutil.common.CommonSecondTimer
import net.jkcode.jkutil.common.newPeriodic
import net.jkcode.jkutil.common.output
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

        // output eg. tcp_tcpserver:2/2
        val text = pro.output()
        if(text.isNullOrBlank())
            return null

        // 2 parse service and replicas
        val services = HashMap<String, Int>()
        for (line in text.split("\n")) {
            if(line.isEmpty())
                break
            // tcp_tcpserver:2/2
            var (service, replicas) = line.split(':')
            replicas = replicas.substringAfter('/')
            services[service] = replicas.toInt()
        }
        return services
    }

    /**
     * 启动定时查询swarm服务的节点数
     * @param timerSeconds
     */
    public fun start(timerSeconds: Long = 10){
        CommonSecondTimer.newPeriodic({
            // 查询swarm服务的节点数
            val data = querySwarmServiceReplicas()
            if(data.isNullOrEmpty()) // 无服务
                clientLogger.error("查询swarm服务的节点数为空, 请检查当前主机是否docker管理节点, 并执行命令查看是否有服务: {}", queryCmd)
            else // 有服务：广播消息
                SwarmUtil.mqMgr.sendMq(SwarmUtil.topic, data)
        }, timerSeconds, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start()
    }
}