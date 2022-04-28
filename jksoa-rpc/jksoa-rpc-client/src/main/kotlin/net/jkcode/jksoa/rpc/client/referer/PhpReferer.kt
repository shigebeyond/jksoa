package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.registry.IRegistry
import php.runtime.reflection.ClassEntity

/**
 * 服务的php引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-2-14 9:52 AM
 */
class PhpReferer(phpClass:ClassEntity /* 接口类 */): Referer(phpClass.nativeClass, Unit, false) {

    /**
     * 服务代理
     */
    public override val service: Any
        get() = throw UnsupportedOperationException("php引用不支持直接获得服务代理")

    companion object{

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = IRegistry.instance("zk")

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param clazzName
         * @param local 限制本地服务
         * @return
         */
        internal fun <T> getRefer(clazzName: String, local: Boolean = false): T {
            val referer = RefererLoader.get(clazzName) as PhpReferer?
            if(referer == null)
                throw RpcClientException("未加载远程服务: " + clazzName)
            if(local && !referer.local) // 限制本地服务
                throw RpcClientException("没有本地服务: " + clazzName)
            return referer.service as T
        }

    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        if(!local) {
            clientLogger.debug("Referer.close(): 取消监听服务变化")
            registry.unsubscribe(serviceId, IConnectionHub.instance(`interface`))
        }
    }
}
