package net.jkcode.jksoa.rpc.client.swarm.server

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.swarm.SwarmUtil

/**
 * swarm server解析器
 *   从rpc请求(rpc服务类)中,解析出swarm服务名(server:协议ip端口)
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
interface IServerResolver {

    /**
     * 解析swarm服务名(server)
     * @param req
     * @return 协议ip端口(server)
     */
    fun resovleServer(req: IRpcRequest): String{
        val server = resovleServer(req.serviceId)
        if(server == null)
            throw RpcClientException("无法根据服务类[${req.serviceId}]定位swarm server")

        return fixServer(server)
    }

    /**
     * 解析swarm服务名(server)
     * @param serviceId
     * @return 协议ip端口(server)
     */
    fun resovleServer(serviceId: String): String?

    /**
     * 修正server路径
     */
    fun fixServer(server: String): String {
        // 1 自身是`协议://ip:端口`
        if (server.contains("://"))
            return server

        // 2 只有ip，转为`协议://ip:端口`
        return SwarmUtil.swarmServer2Url(server).serverAddr
    }
}