package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import php.runtime.env.Environment
import java.lang.reflect.Method

/**
 * 服务的php引用（代理）
 *
 *   有2个实现类
 *   1 PhpReferer -- php client调用java服务
 *   2 P2pReferer -- php client调用php服务
 *
 *   注意：
 *   1. 因为没有java接口类，因此不支持 service/getMethod()
 *   2. 也不能使用 RefererLoader 来加载与获得服务引用, 因为 RefererLoader 要扫描java服务接口类
 *   3. 私有构造函数, 只能通过 PhpReferer.getOrPutRefer(phpClazzName, env) 来获得实例
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-2-14 9:52 AM
 */
open class IPhpReferer(public val env: Environment, override val serviceId: String /* 服务标识 */) : IReferer {

    /**
     * 服务代理
     */
    public override val service: Any
        get() = throw UnsupportedOperationException("php引用不支持直接获得服务代理对象")

    /**
     * 引用的方法
     *    key是方法名，value是方法
     */
    protected lateinit var refererMethods: Map<String, PhpRefererMethod>

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    override fun getMethod(methodSignature: String): Method? {
        throw UnsupportedOperationException("php引用不支持直接获得方法")
    }

    override fun close() {
    }

    /**
     * 获得引用的方法
     */
    public fun getRefererMethod(methodName: String): PhpRefererMethod {
        return refererMethods[methodName] ?: throw NoSuchMethodException("服务[$serviceId]无方法[${methodName}]")
    }
}