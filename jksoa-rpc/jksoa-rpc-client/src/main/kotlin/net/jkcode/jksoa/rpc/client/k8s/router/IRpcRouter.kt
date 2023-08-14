package net.jkcode.jksoa.rpc.client.k8s.router

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.k8s.K8sUtil


/**
 * rpc路由器(解析k8s server)
 *   从rpc请求(rpc服务类)中,解析出k8s应用域名(server:协议ip端口)
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
interface IRpcRouter {

    companion object{
        /**
         * 路由标签名
         */
        const val ROUTE_TAG_NAME: String = "ROUTE_TAG"
    }

    /**
     * 解析k8s应用域名(server)
     * @param req
     * @return 协议ip端口(server)
     */
    fun resovleServer(req: IRpcRequest): String?
}