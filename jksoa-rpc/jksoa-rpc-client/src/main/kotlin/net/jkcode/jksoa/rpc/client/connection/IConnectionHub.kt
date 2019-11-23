package net.jkcode.jksoa.rpc.client.connection

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.getConstructorOrNull
import net.jkcode.jkutil.common.getOrPutOnce
import net.jkcode.jkutil.common.isSuperClass
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.loadbalance.ILoadBalancer
import net.jkcode.jksoa.rpc.registry.IDiscoveryListener
import java.util.concurrent.ConcurrentHashMap

/**
 * 某个service的rpc连接集中器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
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
         * rpc连接集中器实例池: <服务类 to 实例>
         */
        protected val instances: ConcurrentHashMap<Class<*>, IConnectionHub> = ConcurrentHashMap();

        /**
         * 根据服务类来获得单例
         *
         * @param serviceClass 服务类
         * @return
         */
        public fun instance(serviceClass: Class<*>): IConnectionHub{
            return instances.getOrPutOnce(serviceClass) {
                val annotation = serviceClass.remoteService
                if(annotation == null)
                    throw IllegalArgumentException("Service Class must has annotation @remoteService")

                // 1 获得 IConnectionHub实现类
                var clazz = annotation.connectionHubClass
                if (clazz == Void::class || clazz == Unit::class)
                    clazz = ConnectionHub::class

                // 检查是否 IConnectionHub子类
                if (!IConnectionHub::class.java.isSuperClass(clazz.java))
                    throw RpcClientException("Class [${clazz}] is not a sub class from [IConnectionHub]")

                // 检查默认构造函数
                if (clazz.java.getConstructorOrNull() == null)
                    throw RpcClientException("Class [${clazz}] has no no-arg constructor") // IConnectionHub子类${clazz}无默认构造函数

                // 2 实例化
                val inst = clazz.java.newInstance() as IConnectionHub

                // 3 设置属性
                // 设置服务标识
                inst.serviceId = serviceClass.name
                // 设置均衡负载器
                var loadBalancer = annotation.loadBalancer
                if (loadBalancer != "")
                    inst.loadBalancer = ILoadBalancer.instance(loadBalancer)

                inst
            }
        }

        /**
         * 根据服务类来获得单例
         *
         * @param serviceClass 服务类
         * @return
         */
        public fun instance(serviceClass: String): IConnectionHub{
            return instance(Class.forName(serviceClass))
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