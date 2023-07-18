package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.loader.BaseServiceClass
import net.jkcode.jksoa.rpc.client.IReferer

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

    override fun close() {
    }
}
