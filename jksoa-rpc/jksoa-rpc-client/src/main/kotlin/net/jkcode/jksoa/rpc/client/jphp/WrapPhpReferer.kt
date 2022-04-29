package net.jkcode.jksoa.rpc.client.jphp

import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.ext.java.JavaMethod
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.lang.BaseObject
import php.runtime.memory.ArrayMemory
import php.runtime.memory.ObjectMemory
import php.runtime.memory.StringMemory
import php.runtime.reflection.ClassEntity

/**
 * 包装服务的php引用对象
 * 1 调用服务的php引用对象（代理）
 *    仿jphp自带的 JavaObject，但该类并不能动态调用方法
 *    动态调用方法的实现，使用魔术方法
 *    注意：不能直接使用`dispatcher.dispatch(req)`来发rpc请求，必须通过PhpReferer/PhpRefererMethod来发，以便`PhpRefererMethod.invokeArgs()`根据代理的java方法将php参数转换为正确的java参数类型
 * 2 使用
 *    java中的实例化： val ref = WrapPhpReferer.of(env, "net\\jkcode\\jksoa\\rpc\\example\\ISimpleService")
 *    php中的实例化: $ref = new PhpReferer("net\\jkcode\\jksoa\\rpc\\example\\ISimpleService");
 *    php中的方法调用（默认方法）: $ref->ping();
 */
@Reflection.Name("PhpReferer")
@Reflection.Namespace(JksoaRpcExtension.NS)
open class WrapPhpReferer(env: Environment, clazz: ClassEntity) : BaseObject(env, clazz) {

    // 被包装的服务的引用对象
    lateinit var referer: PhpReferer

    @Reflection.Signature
    fun __construct(env: Environment, phpClazzName: String): Memory {
        this.referer = getRef(phpClazzName, env)
        return Memory.NULL
    }

    //__call()实现一： wrong, 不严谨，因为jphp自动转换的实参类型（如数字会转为Long），可能对不上方法的形参类型(如int)
    /*@Reflection.Signature
    fun __call(name: String, vararg args: Any?): Any? {
        try {
            // 获得方法
            val method = obj.javaClass.getMethodByName(name)
            if(method == null)
                throw NoSuchMethodException("类[${obj.javaClass}]无方法[$name]")
            // 调用方法
            return method.invoke(obj, *args)
        } catch (e: Exception) {
            JavaReflection.exception(env, e)
        }
        return Memory.NULL
    }*/

    //__call()实现二： 使用 JavaMethod 包装方法调用
    @Reflection.Signature(value = [Reflection.Arg("name"), Reflection.Arg("arguments")])
    fun __call(env: Environment, vararg args: Memory): Memory {
        try {
            // 第一个参数是方法名
            val name = args[0].toString()
            // 获得方法 PhpRefererMethod
            val method = referer.getRefererMethod(name)
            if(method == null)
                throw NoSuchMethodException("类[${referer.serviceId}]无方法[$name]")
            // 其他参数是方法参数
            val params = args[1].toValue(ArrayMemory::class.java).values() // 第二个是参数数组
            // 调用方法 PhpRefererMethod
            return method.invoke(env, *params)
        } catch (e: Exception) {
            JavaReflection.exception(env, e)
        }
        return Memory.NULL
    }

    @Reflection.Signature
    fun getClassName(env: Environment, vararg args: Memory): Memory {
        return StringMemory(referer.serviceId)
    }

    companion object {

        /**
         * 创建 PReferer 实例
         */
        fun of(env: Environment, clazzName: String): WrapPhpReferer {
            val javaObject = WrapPhpReferer(env, env.fetchClass(JksoaRpcExtension.NS + "\\JavaReferer"))
            javaObject.referer = getRef(clazzName, env)
            return javaObject
        }

        /**
         * 获得被包装的php引用对象
         */
        private fun getRef(phpClazzName: String, env: Environment): PhpReferer {
            return PhpReferer.getOrPutRefer(phpClazzName, env)
        }
    }
}