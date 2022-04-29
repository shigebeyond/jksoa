package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.rpc.registry.IRegistry
import net.jkcode.jkutil.common.getOrPutOnce
import net.jkcode.jkutil.common.resultFromFuture
import net.jkcode.jkutil.fiber.AsyncCompletionStage
import php.runtime.env.Environment
import php.runtime.reflection.ClassEntity
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

/**
 * 服务的php引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 *   注意：
 *   1. 因为没有java接口类，因此不支持 service/getMethod()
 *   2. 也不能使用 RefererLoader 来加载与获得服务引用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-2-14 9:52 AM
 */
class PhpReferer(protected val env: Environment, internal val phpClass: ClassEntity /* 接口类 */) : IReferer {

    /**
     * 服务标识
     */
    override val serviceId: String = phpClass.name.replace("\\", ".")

    /**
     * 服务代理
     */
    public override val service: Any
        get() = throw UnsupportedOperationException("php引用不支持直接获得服务代理对象")

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    override fun getMethod(methodSignature: String): Method? {
        throw UnsupportedOperationException("php引用不支持直接获得方法")
    }

    protected val refererMethods:ConcurrentHashMap<String, PhpRefererMethod> = ConcurrentHashMap()

    /**
     * 获得引用的方法
     */
    public fun getRefererMethod(methodName: String): PhpRefererMethod {
        return refererMethods.getOrPutOnce(methodName){
            PhpRefererMethod(env, this, methodName)
        }
    }

    companion object {

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = IRegistry.instance("zk")

        /**
         * 服务引用缓存
         *   key为服务标识，即接口类全名
         *   value为服务引用
         */
        protected val refers:ConcurrentHashMap<String, PhpReferer> = ConcurrentHashMap()

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param phpClassName
         * @return
         */
        internal fun getOrPutRefer(phpClassName: String, env: Environment): PhpReferer {
            return refers.getOrPutOnce(phpClassName){
                val phpClass = env.fetchClass(phpClassName) ?: throw RpcClientException("类不存在: " + phpClassName)
                PhpReferer(env, phpClass)
            }
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param phpClass
         * @return
         */
        fun getRefer(phpClass: ClassEntity): PhpReferer? {
            return refers[phpClass.name]
        }
    }

    init {
        // 监听服务变化
        clientLogger.debug("Referer监听服务[{}]变化", serviceId)
        Referer.registry.subscribe(serviceId, IConnectionHub.instance(serviceId))
    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        clientLogger.debug("Referer.close(): 取消监听服务变化")
        registry.unsubscribe(serviceId, IConnectionHub.instance(serviceId))
    }

}
