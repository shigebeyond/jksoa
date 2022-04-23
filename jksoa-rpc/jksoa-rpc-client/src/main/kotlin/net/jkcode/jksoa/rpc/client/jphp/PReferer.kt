package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jkutil.common.getMethodByName
import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.env.TraceInfo
import php.runtime.ext.java.JavaClass
import php.runtime.ext.java.JavaMethod
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.lang.BaseWrapper
import php.runtime.memory.ObjectMemory
import php.runtime.memory.StringMemory
import php.runtime.memory.support.MemoryUtils
import php.runtime.memory.support.MemoryUtils.Converter
import php.runtime.reflection.ClassEntity

/**
 * 包装服务的引用对象
 * 1 调用服务的引用对象（代理）
 *    仿jphp自带的 JavaObject，但该类并不能动态调用方法
 *    动态调用方法的实现，使用魔术方法
 *    注意：不能直接使用`dispatcher.dispatch(req)`来发rpc请求，必须通过调用引用（代理）对象来发，以便`JavaMethod.invokeArgs()`根据代理的java方法将php参数转换为正确的java参数类型
 * 2 使用
 *    java中的实例化： val ref = PReferer.of(env, "net.jkcode.jksoa.rpc.example.ISimpleService")
 *    php中的实例化: $ref = new Referer("net.jkcode.jksoa.rpc.example.ISimpleService");
 *    php中的方法调用（默认方法）: $ref->ping();
 */
@Reflection.Name("Referer")
@Reflection.Namespace(JksoaRpcExtension.NS)
open class PReferer(env: Environment, clazz: ClassEntity) : BaseWrapper<JavaObject>(env, clazz) {

    // 被包装的服务的引用对象
    lateinit var referer: Referer

    @Reflection.Signature
    fun __construct(referer: Referer): Memory {
        this.referer = referer
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
            // 获得方法
            val method = referer.`interface`.getMethodByName(name)
            if(method == null)
                throw NoSuchMethodException("类[${referer.`interface`}]无方法[$name]")
            // 用 JavaMethod 包装方法调用
            val method2 = JavaMethod.of(env, method)
            val args2 = args.toMutableList()
            args2[0] = ObjectMemory(JavaObject.of(env, referer.service)) // 第一个参数，原来是方法名，现替换为被包装的服务的引用对象
            return method2.invokeArgs(env, *args2.toTypedArray())
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

        // 创建 PReferer 实例
        fun of(env: Environment, clazzName: String): PReferer {
            val javaObject = PReferer(env, env.fetchClass("php\\lang\\Referer"))
            val referer = RefererLoader.get(clazzName) as Referer?
            if(referer == null)
                throw RpcClientException("未加载远程服务: " + clazzName)
            javaObject.referer = Referer.getRefer(clazzName)
            return javaObject
        }
    }
}