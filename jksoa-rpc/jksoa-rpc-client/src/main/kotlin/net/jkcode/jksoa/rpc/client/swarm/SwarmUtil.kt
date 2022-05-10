package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jkutil.common.indexAtTimes

/**
 * rpc服务类对swarm服务名(server)的映射
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object SwarmUtil {

    public val mqMgr = IMqManager.instance("kafka")

    public val topic = "SwarmServiceReplicas"

    /**
     * rpc服务类转为swarm服务名(server)
     *   根据rpc服务类的包路径的前3节，就是服务名，如 rpc请求为`com.sk.order.OrderSerivce#createOrder()`，前三节为服务名`com.sk.order`
     * @param serviceClass
     */
    fun serviceClass2swarmServer(serviceClass: String): String {
        val pos = serviceClass.indexAtTimes('.', 3) // 前三节的位置
        return serviceClass.substring(0, pos) // 前三节的子串
    }

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

/**
 * 获得swarm服务名(server)
 */
public val IRpcRequest.swarmServer: String
    get() = SwarmUtil.serviceClass2swarmServer(this.serviceId)