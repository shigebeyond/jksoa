package com.jksoa.client.connection

import com.jksoa.client.IConnection
import com.jksoa.common.IRpcRequest
import com.jksoa.registry.IDiscoveryListener

/**
 * rpc连接集中器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-15 9:25 AM
 */
interface IConnectionHub: IDiscoveryListener {

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    fun select(req: IRpcRequest): IConnection

    /**
     * 选择全部连接
     *
     * @param serviceId 服务标识，即接口类全名
     * @return
     */
    fun selectAll(serviceId: String): Collection<IConnection>
}