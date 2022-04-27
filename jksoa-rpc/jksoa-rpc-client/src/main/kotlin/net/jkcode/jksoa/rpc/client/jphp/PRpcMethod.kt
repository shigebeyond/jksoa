package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jkutil.common.getMethodByName
import net.jkcode.jkutil.common.substringBetween
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
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.ClassEntity
import java.lang.reflect.InvocationTargetException

/**
 * 包装远程方法
 * 1 调用远程方法
 * 2 使用
 *    java中的实例化： val ref = PReferer.of(env, "net.jkcode.jksoa.rpc.example.ISimpleService")
 *    php中的实例化: $ref = new Referer("net.jkcode.jksoa.rpc.example.ISimpleService");
 *    php中的方法调用（默认方法）: $ref->ping();
 */
@Reflection.Name("Referer")
@Reflection.Namespace(JksoaRpcExtension.NS)
open class PDynReferer(env: Environment, clazz: ClassEntity) : BaseObject(env) {

    protected lateinit var clazz: String // 远程类
    protected lateinit var methodSignature: String // 远程方法
    protected lateinit var paramTypes: Array<Class<*>> // 参数类型
    protected lateinit var converters: Array<MemoryUtils.Converter<*>> // 参数转换器
    protected var returnType: Class<*> = Void.TYPE // 返回值类型
    protected var resultConverter: MemoryUtils.Converter<*>? = null // 返回值转换器

    @Reflection.Signature
    fun __construct(clazz: String, methodSignature: String): Memory {
        this.clazz = clazz
        this.methodSignature = methodSignature
        // 解析参数类型+返回值类型 TODO: 未处理泛型
        // 获得参数片段：括号包住
        val paramString = methodSignature.substringBetween('(', ')')
        // 解析参数类型
        paramTypes = paramString.split(",\\s*").map {
            Class.forName(it.trim())
        }.toTypedArray()
        converters = MemoryUtils.getConverters(paramTypes)
        // 获得返回值类型：空格之前
        val returnString = methodSignature.substringBefore(' ', "").trim()
        if(returnString.isNotEmpty()){
            returnType = Class.forName(returnString)
            resultConverter = MemoryUtils.getConverter(returnType)
        }
        return Memory.NULL
    }

    @Reflection.Signature
    public fun invokeArgs(env: Environment, vararg args: Memory): Memory? {
        val tmp = args[1].toValue(ArrayMemory::class.java).values()
        val passed = arrayOfNulls<Memory>(tmp.size + 1)
        System.arraycopy(tmp, 0, passed, 1, tmp.size)
        passed[0] = args[0]
        return invoke(env, *(passed as Array<Memory>))
    }

    @Reflection.Signature
    public operator fun invoke(env: Environment, vararg args: Memory): Memory? {
        val len = args.size - 1
        if (len < paramTypes.size || len > paramTypes.size)
            JavaReflection.exception(env, IllegalArgumentException("Invalid argument count"))
        val passed = arrayOfNulls<Any>(len)
        var i = 0
        for (converter in converters) {
            val arg = args[i + 1]
            if (arg.instanceOf("php\\lang\\JavaObject")) {
                passed[i] = (arg.toValue(ObjectMemory::class.java).value as JavaObject).getObject()
            } else {
                if (converter != null) {
                    passed[i] = converter.run(args[i + 1])
                } else {
                    passed[i] = null
                }
            }
            i++
        }
        val obj = if (args[0].isNull) null else (args[0].toValue(ObjectMemory::class.java).value as JavaObject).getObject()
        return doInvoke(env, obj, passed)
    }

    /**
     * 真正的发起rpc调用
     *   TODO: 改造 RpcInvocationHandler
     */
    protected fun doInvoke(env: Environment, obj: Any?, passed: Array<Any?>): Memory {
        try {
            val result: Any = method.invoke(obj, *passed) ?: return Memory.NULL
            if (resultConverter != null)
                return MemoryUtils.valueOf(result)

            if (Void.TYPE == Void.TYPE)
                return Memory.NULL

            return ObjectMemory(JavaObject.of(env, result))
        } catch (e: IllegalAccessException) {
            JavaReflection.exception(env, e)
        } catch (e: InvocationTargetException) {
            JavaReflection.exception(env, e.targetException)
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
        fun of(env: Environment, clazzName: String): PReferer {
            val javaObject = PReferer(env, env.fetchClass(JksoaRpcExtension.NS + "\\Referer"))
            javaObject.referer = getRef(clazzName)
            return javaObject
        }

        /**
         * 获得被包装的服务引用对象
         */
        private fun getRef(clazzName: String): Referer {
            val referer = RefererLoader.get(clazzName) as Referer?
            if (referer == null)
                throw RpcClientException("未加载远程服务: " + clazzName)
            return referer
        }
    }
}

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
open class `PDynReferer.kt`(env: Environment, clazz: ClassEntity) : BaseObject(env) {

    // 被包装的服务的引用对象
    lateinit var referer: Referer

    @Reflection.Signature
    fun __construct(clazzName: String): Memory {
        this.referer = getRef(clazzName)
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

        /**
         * 创建 PReferer 实例
         */
        fun of(env: Environment, clazzName: String): `PDynReferer.kt` {
            val javaObject = `PDynReferer.kt`(env, env.fetchClass(JksoaRpcExtension.NS + "\\Referer"))
            javaObject.referer = getRef(clazzName)
            return javaObject
        }

        /**
         * 获得被包装的服务引用对象
         */
        private fun getRef(clazzName: String): Referer {
            val referer = RefererLoader.get(clazzName) as Referer?
            if (referer == null)
                throw RpcClientException("未加载远程服务: " + clazzName)
            return referer
        }
    }
}