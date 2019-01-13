package com.jksoa.loadbalance

import com.jkmvc.common.get
import com.jkmvc.common.randomInt
import com.jksoa.common.IRpcRequest

/**
 * 随机的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:21
 **/
class RandomLoadBalanceStrategy : ILoadBalanceStrategy {
    /**
     * 选择节点
     *
     * @param node
     * @param req
     * @return 选中的节点序号
     */
    public override fun select(nodes: Collection<INode>, req: IRpcRequest): Int {
        if(nodes.isEmpty())
            return -1

        // 随机选个节点
        return randomInt(nodes.size)
    }
}