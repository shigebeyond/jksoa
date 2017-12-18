package com.jksoa.loadbalance

import com.jkmvc.common.getRandom
import com.jksoa.common.Request

/**
 * 随机的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:21
 **/
class RandomLoadBalance : ILoadBalance {
    /**
     * 选择节点
     *
     * @param node
     * @param req
     * @return
     */
    public override fun select(nodes: Collection<INode>, req: Request): INode? {
        if(nodes.isEmpty())
            return null

        // 随机选个节点
        return nodes.getRandom()
    }
}