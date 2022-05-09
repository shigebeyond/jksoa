package net.jkcode.jksoa.rpc.client.connection

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.loadbalance.ILoadBalancer
import net.jkcode.jksoa.rpc.registry.IDiscoveryListener
import net.jkcode.jkutil.common.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 某个service的rpc连接集中器
 *    1 维系client对所有server的所有连接
 *    2 在client调用中对server集群进行均衡负载
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
        protected val instances: ConcurrentHashMap<String, IConnectionHub> = ConcurrentHashMap();

        /**
         * 根据服务类来获得单例
         *
         * @param serviceClassName 服务类，兼容php引用的情况，注意不支持自定义ConnectionHub类
         * @return
         */
        public fun instance(serviceClassName: String): IConnectionHub{
            return instances.getOrPutOnce(serviceClassName) {
                // 1 获得ConnectionHub类
                // 1.1 服务类
                var serviceClass: Class<*>? = null
                try {
                    // java引用
                    serviceClass = getClassByName(serviceClassName)
                }catch (ex: ClassNotFoundException){
                    // 如果是php引用，则不存在服务接口类，还是最大可能尝试调用
                }

                // 1.2 服务注解
                val annotation = serviceClass?.remoteService
                if(serviceClass != null && annotation == null) // java引用
                    throw IllegalArgumentException("Service interface must has annotation @RemoteService")

                // 1.3 ConnectionHub类
                var clazz: KClass<*> = getConnectionHubClass(annotation) ?: ConnectionHub::class

                // 2 实例化
                val inst = clazz.java.newInstance() as IConnectionHub

                // 3 设置属性
                // 设置服务标识
                inst.serviceId = serviceClassName
                // 设置均衡负载器
                var loadBalancer = annotation?.loadBalancer
                if (!loadBalancer.isNullOrEmpty())
                    inst.loadBalancer = ILoadBalancer.instance(loadBalancer)

                inst
            }
        }

        /**
         * 获得ConnectionHub类
         * @param annotation 服务注解，如果为null则是php引用，否则为java引用
         */
        private fun getConnectionHubClass(annotation: RemoteService?): KClass<*> {
            // 1 docker swarm模式下的引用

            // 2 php引用
            if (annotation == null)
                return ConnectionHub::class

            // 3 java引用
            // 获得 IConnectionHub实现类
            val clazz = annotation.connectionHubClass
            if (clazz == Void::class || clazz == Unit::class)
                return ConnectionHub::class

            // 检查是否 IConnectionHub子类
            if (!IConnectionHub::class.java.isSuperClass(clazz.java))
                throw RpcClientException("Class [${clazz}] is not a sub class from [IConnectionHub]")

            // 检查默认构造函数
            if (clazz.java.getConstructorOrNull() == null)
                throw RpcClientException("Class [${clazz}] has no no-arg constructor") // IConnectionHub子类${clazz}无默认构造函数

            return clazz
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