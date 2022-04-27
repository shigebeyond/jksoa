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
@Reflection.Name("RpcMethod")
@Reflection.Namespace(JksoaRpcExtension.NS)
open class PRpcMethod(env: Environment, clazz: ClassEntity) : BaseObject(env) {

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
        return StringMemory(clazz)
    }

    companion object {

        /**
         * 创建 PRpcMethod 实例
         */
        fun of(env: Environment, clazzName: String, methodSignature: String): PRpcMethod {
            val javaObject = PRpcMethod(env, env.fetchClass(JksoaRpcExtension.NS + "\\RpcMethod"))
            javaObject.__construct(clazzName, methodSignature)
            return javaObject
        }

    }
}