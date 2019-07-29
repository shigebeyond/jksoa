package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection

/**
 * 一致性hash的均衡负载算法
 *
 * @author shijianhang
 * @create 2019-7-18 下午9:21
 **/
class ConsistentHashLoadBalancer : ILoadBalancer {
    /**
     * 选择连接
     *    TODO: 添加权重因子 IConnection.weight
     *
     * @param conn
     * @param req
     * @return
     */
    public override fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection? {
        if(conns.isEmpty())
            return null

        // 一致性哈希
        return ConsistentHash(3, 100, conns).get(req)
    }
}