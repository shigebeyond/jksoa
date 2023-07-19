package net.jkcode.jksoa.rpc.client.k8s

import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jksoa.common.Url
import net.jkcode.jkutil.common.Config

/**
 * k8s模式工具类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object K8sUtil {

    /**
     * 配置
     */
    public val config: Config = Config.instance("rpc-client", "yaml")

    /**
     * mq管理者，一般是kafka实现
     */
    public val mqMgr = IMqManager.instance(config["k8sMqType"]!!, config["k8sMqName"]!!)

    /**
     * 服务发现的消息主题
     */
    public val topic = "K8sServiceReplicas"

    /**
     * k8s服务名(server)转url
     *    默认协议jkrp跟端口9080
     * @param server
     * @param replicas 服务副本数, 即server数
     * @return
     */
    fun k8sServer2Url(server: String, replicas: Int? = null): Url {
        val params = if(replicas == null)
                        emptyMap()
                    else
                        mapOf("replicas" to replicas)
        return Url("jkr", server,9080, "", params)
    }
}