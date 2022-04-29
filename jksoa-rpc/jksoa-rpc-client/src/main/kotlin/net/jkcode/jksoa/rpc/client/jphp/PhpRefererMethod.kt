package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jkutil.common.substringBetween
import net.jkcode.jkutil.fiber.AsyncCompletionStage
import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.lang.BaseObject
import php.runtime.memory.ArrayMemory
import php.runtime.memory.ObjectMemory
import php.runtime.memory.StringMemory
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.ClassEntity
import php.runtime.reflection.MethodEntity
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * 包装远程方法
 *   负责参数类型+返回值类型转换
 */
open class PhpRefererMethod(env: Environment, protected val phpRef: PhpReferer, protected val phpMethod: MethodEntity) : BaseObject(env) {

    constructor(env: Environment, phpRef: PhpReferer, phpMethod: String): this(env, phpRef, phpRef.phpClass.findMethod(phpMethod))

    protected lateinit var methodSignature: String // java远程方法
    protected lateinit var paramTypes: Array<Class<*>> // 参数类型
    protected lateinit var converters: Array<MemoryUtils.Converter<*>> // 参数转换器
    protected var returnType: Class<*> = Void.TYPE // 返回值类型
    protected var resultConverter: MemoryUtils.Converter<*>? = null // 返回值转换器

    init {
        // 直接调用映射的php方法，结果即为java方法签名(带返回值类)
        val parts = phpMethod.invokeStatic(env).toString().split(' ', limit = 2)
        methodSignature = parts[1]
        // 解析参数类型+返回值类型 TODO: 未处理泛型
        // 获得参数片段：括号包住
        val paramString = methodSignature.substringBetween('(', ')')
        // 解析参数类型
        paramTypes = paramString.split(",\\s*").map {
            Class.forName(it.trim())
        }.toTypedArray()
        converters = MemoryUtils.getConverters(paramTypes)
        // 获得返回值类型：空格之前
        val returnString = parts[0]
        if(returnString.isNotEmpty()){
            returnType = Class.forName(returnString)
            resultConverter = MemoryUtils.getConverter(returnType)
        }
    }

    public fun invokeArgs(env: Environment, vararg args: Memory): Memory? {
        val tmp = args[1].toValue(ArrayMemory::class.java).values() // 第二个是参数数组
        val passed = arrayOfNulls<Memory>(tmp.size + 1)
        System.arraycopy(tmp, 0, passed, 1, tmp.size)
        passed[0] = args[0] // 第一个是this对象
        return invoke(env, *(passed as Array<Memory>))
    }

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
        try {
            val result: Any = doInvoke(passed) ?: return Memory.NULL
            if (resultConverter != null)
                return MemoryUtils.valueOf(result)

            return when(returnType){
                Void.TYPE -> Memory.NULL
                CompletableFuture::class.java -> ObjectMemory(WrapCompletableFuture(env, result as CompletableFuture<*>))
                else -> ObjectMemory(JavaObject.of(env, result))
            }
        } catch (e: IllegalAccessException) {
            JavaReflection.exception(env, e)
        } catch (e: InvocationTargetException) {
            JavaReflection.exception(env, e.targetException)
        }
        return Memory.NULL
    }

    /**
     * 守护之后真正的调用
     *    将方法调用转为发送rpc请求
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    public fun doInvoke(args: Array<Any?>): Any? {
        // 1 封装请求
        val req = RpcRequest(phpRef.serviceId, methodSignature, args)

        // 2 分发请求, 获得异步响应
        val resFuture = RpcInvocationHandler.invoke(req)

        // 1 异步结果
        //if (Future::class.java.isAssignableFrom(method.returnType))
        if(returnType == Future::class.java
                || returnType == CompletableFuture::class.java)
            return resFuture

        // 2 同步结果
        //return resFuture.get()
        return AsyncCompletionStage.get(resFuture) // 支持协程
    }

}