package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.loader.BaseServiceClass
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.registry.IRegistry

/**
 * 服务的引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
open class Referer(public override val `interface`:Class<*>, // 接口类
                  public override val service: Any = RpcInvocationHandler.createProxy(`interface`), // 服务实例，默认是服务代理，但在服务端可指定本地服务实例
                  public val local: Boolean = false // 是否本地服务
): BaseServiceClass(), IReferer {

    companion object{

        /**
         * 配置了注册中心
         */
        public val registryOrSwarm: Boolean = RefererLoader.config["registryOrSwarm"]!!

        /**
         * 注册中心
         *   TODO: 支持多个配置中心, 可用组合模式
         *   如果registryOrSwarm为false, 根本不需要注册中心, 因此延迟创建
         */
        public val registry: IRegistry by lazy {
            IRegistry.instance("zk")
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param clazzName
         * @param local 限制本地服务
         * @return
         */
        internal fun <T> getRefer(clazzName: String, local: Boolean = false): T {
            val referer = RefererLoader.get(clazzName)
            if(referer == null)
                throw RpcClientException("未加载远程服务: " + clazzName)
            if(local && !referer.local) // 限制本地服务
                throw RpcClientException("没有本地服务: " + clazzName)
            return referer.service as T
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param clazz
         * @param local 限制本地服务
         * @return
         */
        @JvmStatic
        @JvmOverloads
        public fun <T> getRefer(clazz: Class<T>, local: Boolean = false): T {
            return getRefer(clazz.name, local)
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param local 限制本地服务
         * @return
         */
        public inline fun <reified T> getRefer(local: Boolean = false): T {
            return getRefer(T::class.java, local)
        }
    }

    init {
        if((!local) && registryOrSwarm) {
            // 监听服务变化
            clientLogger.debug("Referer监听服务[{}]变化", serviceId)
            registry.subscribe(serviceId, IConnectionHub.instance(serviceId))
        }
    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        if((!local) && registryOrSwarm) {
            clientLogger.debug("Referer.close(): 取消监听服务变化")
            registry.unsubscribe(serviceId, IConnectionHub.instance(serviceId))
        }
    }
}
