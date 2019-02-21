package com.jksoa.loadbalance

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
     * @return 选中的连接序号
     */
    public override fun select(conns: Collection<IConnection>): Int {
        if(conns.isEmpty())
            return -1

        // 随机选个连接
        return randomInt(conns.size)
    }
}