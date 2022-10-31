package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jphp.ext.isDegradeFallbackMethod
import net.jkcode.jphp.ext.isUnregistered
import php.runtime.env.Environment
import php.runtime.reflection.ClassEntity

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
class PhpReferer protected constructor(env: Environment, public val phpClass: ClassEntity /* 接口类 */) : IPhpReferer(env, phpClass.name.replace("\\", ".")) {

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

    companion object {

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

}
