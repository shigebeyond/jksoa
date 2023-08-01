package net.jkcode.jksoa.rpc.client.connection

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.loadbalance.ILoadBalancer
import net.jkcode.jksoa.rpc.client.k8s.K8sConnectionHub
import net.jkcode.jkutil.common.*

/**
 * 某个service的rpc连接集中器
 *    1 单个service下, 维系client对所有server的所有连接
 *    2 单个service下, 在client调用中对server集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-15 9:25 AM
 */
abstract class IConnectionHub: IDiscoveryListener {

    companion object{
        /**
         * 客户端配置
         */
        public val config = Config.instance("rpc-client", "yaml")

        /**
         * 根据服务类来获得单例
         *
         * @param serviceClassName 服务类，兼容php引用的情况，注意不支持自定义ConnectionHub类
         * @return
         */
        public fun instance(serviceClassName: String): IConnectionHub{
            return K8sConnectionHub// k8s模式: 所有rpc服务共用一个实例
        }

    }

    /**
     * 服务标识，即接口类全名
     */
    public override var serviceId: String = ""
        protected set

    /**
     * 均衡负载算法
     */
    public var loadBalancer: ILoadBalancer = ILoadBalancer.instance(config["loadbalancer"]!!)
        protected set

    /**
     * 根据请求选择一个连接
     *
     * @param req
     * @return
     */
    public abstract fun select(req: IRpcRequest): IConnection

    /**
     * 根据请求选择多个连接
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return 返回多个连接
     */
    public abstract fun selectAll(req: IRpcRequest? = null): Collection<IConnection>
}