package net.jkcode.jksoa.loadbalance

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.client.IConnection

/**
 * 选择连接的均衡负载算法
 * 
 * @author shijianhang
 * @create 2017-12-18 下午9:04
 **/
interface ILoadBalancer {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<ILoadBalancer>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("load-balance-strategy", "yaml")
    }

    /**
     * 选择连接
     *
     * @param conns
     * @param req
     * @return
     */
    fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection?
}
