package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection

/**
 * 随机的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:21
 **/
class RandomLoadBalancer : ILoadBalancer {
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

        // 随机选个连接
        val i = randomInt(conns.size)
        return conns[i]
    }
}