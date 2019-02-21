package com.jksoa.loadbalance

import com.jkmvc.common.get
import com.jkmvc.common.randomInt
import com.jksoa.common.IRpcRequest
import com.jksoa.client.IConnection

/**
 * 随机的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:21
 **/
class RandomLoadBalanceStrategy : ILoadBalanceStrategy {
    /**
     * 选择连接
     *
     * @param conn
     * @return
     */
    public override fun select(conns: Collection<IConnection>): IConnection? {
        if(conns.isEmpty())
            return null

        // 随机选个连接
        val i = randomInt(conns.size)
        return conns[i]
    }
}