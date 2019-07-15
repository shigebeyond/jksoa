package net.jkcode.jksoa.client.referer

import net.jkcode.jksoa.client.IReferer
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.registry.IRegistry
import net.jkcode.jksoa.registry.zk.ZkRegistry

/**
 * 服务的引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
class Referer(public override val `interface`:Class<*> /* 接口类 */,
              public override val service: Any = RpcInvocationHandler.createProxy(`interface`), /* 服务实例，默认是服务代理，但在服务端可指定本地服务实例 */
              public val local: Boolean = false /* 是否本地服务 */
): IReferer() {

    companion object{

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = ZkRegistry

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param clazzName
         * @param local 限制本地服务
         * @return
         */
        internal fun <T> getRefer(clazzName: String, local: Boolean = false): T {
            val referer = RefererLoader.get(clazzName) as Referer?
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
        if(!local) {
            // 监听服务变化
            clientLogger.debug("Referer监听服务[{}]变化", serviceId)
            registry.subscribe(serviceId, ConnectionHub)
        }
    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        if(!local) {
            clientLogger.info("Referer.close(): 取消监听服务变化")
            registry.unsubscribe(serviceId, ConnectionHub)
        }
    }
}
