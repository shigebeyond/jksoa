package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jksoa.rpc.registry.IRegistry
import net.jkcode.jphp.ext.isDegradeFallbackMethod
import net.jkcode.jphp.ext.isUnregistered
import php.runtime.env.Environment
import php.runtime.reflection.ClassEntity
import java.lang.reflect.Method

/**
 * 服务的php引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 *   注意：
 *   1. 因为没有java接口类，因此不支持 service/getMethod()
 *   2. 也不能使用 RefererLoader 来加载与获得服务引用, 因为 RefererLoader 要扫描java服务接口类
 *   3. 私有构造函数, 只能通过 PhpReferer.getOrPutRefer(phpClazzName, env) 来获得实例
 *   4. PhpReferer 实例要缓存到 ClassEntity 中, 1是提高性能 2 是应对php类卸载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-2-14 9:52 AM
 */
class PhpReferer protected constructor(public val env: Environment, public val phpClass: ClassEntity /* 接口类 */) : IReferer {

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

    /**
     * 引用的方法
     *    key是方法名，value是方法
     */
    protected val refererMethods: Map<String, PhpRefererMethod>

    init {
        // 引用远程方法: 不包含的降级的本地方法
        // 1 先获得映射方法
        val mappingMethods = phpClass.methods.values.filter { method ->
            !phpClass.isDegradeFallbackMethod(method.name)
        }
        // 2 根据映射方法来构建引用方法
        refererMethods = mappingMethods.associate { method ->
            method.name to PhpRefererMethod(this, method)
        }
    }

    /**
     * 获得引用的方法
     */
    public fun getRefererMethod(methodName: String): PhpRefererMethod {
        return refererMethods[methodName] ?: throw NoSuchMethodException("服务[$serviceId]无方法[${methodName}]")
    }

    companion object {

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
         *   要缓存到 ClassEntity 中, 1是提高性能 2 是应对php类卸载
         * @param phpClassName
         * @return
         */
        internal fun getOrCreateRefer(phpClassName: String, env: Environment): PhpReferer {
            // 获得php类
            val phpClass = env.fetchClass(phpClassName) ?: throw RpcClientException("php类不存在: " + phpClassName)
            if(phpClass.isUnregistered)
                throw RpcClientException("php类[$phpClassName]已注销")
            // 读php类中的缓存
            return phpClass.getAdditionalData("phpReferer", PhpReferer::class.java){
                PhpReferer(env, phpClass)
            }
        }
    }

    init {
        if(registryOrSwarm) {
            // 监听服务变化
            clientLogger.debug("PhpReferer监听服务[{}]变化", serviceId)
            registry.subscribe(serviceId, IConnectionHub.instance(serviceId))
        }
    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        if(registryOrSwarm) {
            clientLogger.debug("Referer.close(): 取消监听服务变化")
            registry.unsubscribe(serviceId, IConnectionHub.instance(serviceId))
        }
    }

}
