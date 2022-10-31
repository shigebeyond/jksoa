package net.jkcode.jksoa.rpc.client.jphp

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.ext.java.JavaReflection
import php.runtime.lang.BaseObject
import php.runtime.memory.ArrayMemory
import php.runtime.memory.StringMemory
import php.runtime.reflection.ClassEntity

/**
 * 包装服务的php引用对象
 * 1 调用服务的php引用对象（代理）
 *    仿jphp自带的 JavaObject，但该类并不能动态调用方法
 *    动态调用方法的实现，使用魔术方法
 *    注意：不能直接使用`dispatcher.dispatch(req)`来发rpc请求，必须通过P2pReferer/P2pRefererMethod来发，以便`P2pRefererMethod.invokeArgs()`根据代理的java方法将php参数转换为正确的java参数类型
 * 2 使用
 *    java中的实例化： val ref = WrapP2pReferer.of(env, "net\\jkcode\\jksoa\\rpc\\example\\ISimpleService")
 *    php中的实例化: $ref = new P2pReferer("net\\jkcode\\jksoa\\rpc\\example\\ISimpleService");
 *    php中的方法调用（默认方法）: $ref->hostname();
 */
@Reflection.Name("P2pReferer")
@Reflection.Namespace(JksoaRpcExtension.NS)
open class WrapP2pReferer(env: Environment, clazz: ClassEntity) : BaseObject(env, clazz) {

    // 被包装的服务的引用对象
    lateinit var referer: P2pReferer

    @Reflection.Signature
    fun __construct(env: Environment, javaClazzName: String): Memory {
        this.referer = P2pReferer.getOrCreateRefer(javaClazzName, env)
        return Memory.NULL
    }

    //jphp自动转换实参类型（如数字会转为Long）
    @Suspendable
    @Reflection.Signature
    fun callPhpFile(env: Environment, file: StringMemory, args: ArrayMemory): Memory {
        try {
            // 获得方法 PhpRefererMethod
            val method = referer.getRefererMethod("callPhpFile")
            lastCall = file.toString() + ".php"
            // 调用方法 PhpRefererMethod
            val params = arrayOf(file, args)
            return RpcInvocationHandler.guardInvoke(method, this, params, env)
        } catch (e: Exception) {
            JavaReflection.exception(env, e)
        }
        return Memory.NULL
    }

    @Suspendable
    @Reflection.Signature
    fun callPhpFunc(env: Environment, func: StringMemory, args: ArrayMemory): Memory {
        try {
            // 获得方法 PhpRefererMethod
            val method = referer.getRefererMethod("callPhpFunc")
            lastCall = func.toString() + "()"
            // 调用方法 PhpRefererMethod
            val params = arrayOf(func, args)
            return RpcInvocationHandler.guardInvoke(method, this, params, env)
        } catch (e: Exception) {
            JavaReflection.exception(env, e)
        }
        return Memory.NULL
    }

    //上一次调用的方法
    protected var lastCall: String? = null

    @Reflection.Signature
    fun getLastCall(): Memory {
        if(lastCall == null)
            return Memory.NULL

        return StringMemory(lastCall)
    }

    @Reflection.Signature
    fun getClassName(): Memory {
        return StringMemory(referer.serviceId)
    }

    companion object {

        /**
         * 创建 PReferer 实例
         */
        fun of(env: Environment, javaClazzName: String): WrapP2pReferer {
            val wr = WrapP2pReferer(env, env.fetchClass(JksoaRpcExtension.NS + "\\P2pReferer"))
            wr.referer = P2pReferer.getOrCreateRefer(javaClazzName, env)
            return wr
        }

    }
}