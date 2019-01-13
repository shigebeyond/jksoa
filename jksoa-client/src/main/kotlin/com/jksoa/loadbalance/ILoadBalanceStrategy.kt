package com.jksoa.loadbalance

import com.jkmvc.common.Config
import com.jkmvc.common.NamedSingleton
import com.jkmvc.common.IConfig
import com.jksoa.common.IRpcRequest

/**
 * 选择节点的均衡负载算法
 * 
 * @author shijianhang
 * @create 2017-12-18 下午9:04
 **/
interface ILoadBalanceStrategy {

    // 可配置的单例
    companion object: NamedSingleton<ILoadBalanceStrategy>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("load-balance-strategy", "yaml")
    }

    /**
     * 选择节点
     *
     * @param node
     * @param req
     * @return
     */
    fun select(nodes: Collection<INode>, req: IRpcRequest): INode?
}
