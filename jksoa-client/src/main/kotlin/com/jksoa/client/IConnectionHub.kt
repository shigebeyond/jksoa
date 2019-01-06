package com.jksoa.client

import com.jksoa.common.IRequest
import com.jksoa.protocol.IConnection
import com.jksoa.registry.IDiscoveryListener
import java.io.Closeable

/**
 * rpc连接集中器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-15 9:25 AM
 */
interface IConnectionHub: IDiscoveryListener, Closeable {

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    fun select(req: IRequest): IConnection
}