package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jkutil.common.CommonSecondTimer
import net.jkcode.jkutil.common.newPeriodic
import net.jkcode.jkutil.common.output
import java.util.concurrent.TimeUnit

/**
 * docker swarm模式下的服务发现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object SwarmDiscovery {

    /**
     * 通过 docker service ls 命令来查询swarm服务的节点数
     * @return Map<swarm服务名, 节点数>
     */
    private fun querySwarmServiceReplicas(): HashMap<String, Int> {
        // 1 exec comd
        // https://docs.docker.com/engine/reference/commandline/service_ls/
        val cmd = "docker service ls --format {{.Name}}:{{.Replicas}}"
        val pro: Process = Runtime.getRuntime().exec(cmd)
        val status = pro.waitFor()
        if (status != 0)
            throw RpcClientException("Failed to call command: $cmd")

        // output eg. tcp_tcpserver:2/2
        val text = pro.output()

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
    public fun start(timerSeconds: Long = 120){
        CommonSecondTimer.newPeriodic({
            // 查询swarm服务的节点数
            val data = querySwarmServiceReplicas()
            // 发布
            SwarmUtil.mqMgr.sendMq(SwarmUtil.topic, data)
        }, timerSeconds, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start()
    }
}