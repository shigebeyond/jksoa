package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.indexAtTimes

/**
 * docker swarm模式工具类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object SwarmUtil {

    /**
     * 配置
     */
    public val config: Config = Config.instance("rpc-client", "yaml")

    /**
     * mq管理者，一般是kafka实现
     */
    public val mqMgr = IMqManager.instance(config["swarmMqType"]!!, config["swarmMqName"]!!)

    /**
     * 服务发现的消息主题
     */
    public val topic = "SwarmServiceReplicas"

    /**
     * swarm服务名(server)转url
     * @param server
     * @param replicas 服务副本数, 即server数
     * @return
     */
    fun swarmServer2Url(server: String, replicas: Int): Url {
        return Url("jkr", server,9080, "", mapOf("replicas" to replicas))
    }
}