package net.jkcode.jksoa.rpc.client.jphp

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Boolean
import net.jkcode.jkutil.common.getClassByName
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jkutil.common.hasUpperCase
import net.jkcode.jkutil.common.substringBetween
import net.jkcode.jkutil.fiber.AsyncCompletionStage
import php.runtime.Memory
import php.runtime.env.Environment
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.memory.ArrayMemory
import php.runtime.memory.ObjectMemory
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.MethodEntity
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * 包装远程方法
 *   负责参数类型+返回值类型转换(参考 JavaObject 实现)
 */
class PhpRefererMethod(protected val phpRef: PhpReferer, protected val phpMethod: MethodEntity) {

    constructor(phpRef: PhpReferer, phpMethod: String): this(phpRef, phpRef.phpClass.findMethod(phpMethod))

    protected lateinit var methodSignature: String // java远程方法
    protected var paramTypes: Array<Class<*>> = emptyArray() // 参数类型
    protected var converters: Array<MemoryUtils.Converter<*>> = emptyArray() // 参数转换器
    protected var returnType: Class<*> = Void.TYPE // 返回值类型
    protected var resultConverter: MemoryUtils.Converter<*>? = null // 返回值转换器

    init {
        // 直接调用映射的php方法，结果即为java方法签名(带返回值类)
        val parts = phpMethod.invokeStatic(phpRef.env).toString().split(' ', limit = 2)
        methodSignature = parts[1]
        // 解析参数类型+返回值类型 TODO: 未处理泛型
        // 获得参数片段：括号包住
        val paramString = methodSignature.substringBetween('(', ')')
        if(paramString.isNotBlank()) {
            // 解析参数类型
            paramTypes = paramString.split(",\\s*").map {
                val clazzName = it.trim()
                getClassByName(clazzName)
            }.toTypedArray()
            converters = MemoryUtils.getConverters(paramTypes)
        }
        // 获得返回值类型：空格之前
        val returnString = parts[0].trim()
        if(returnString.isNotBlank()){
            returnType = getClassByName(returnString)
            resultConverter = MemoryUtils.getConverter(returnType)
        }
    }

    /**
     * 调用引用方法
     * @param env
     * @param args 方法实参，不包含this或方法名， 因为不需要
     */
    public operator fun invoke(env: Environment, vararg args: Memory): Memory {
        val len = args.size
        if (len < paramTypes.size || len > paramTypes.size)
            JavaReflection.exception(env, IllegalArgumentException("Invalid argument count"))
        // 转换实参
        val passed = arrayOfNulls<Any>(len)
        var i = 0
        for (converter in converters) {
            val arg = args[i]
            if (arg.instanceOf("php\\lang\\JavaObject")) {
                passed[i] = (arg.toValue(ObjectMemory::class.java).value as JavaObject).getObject()
            } else {
                if (converter != null) {
                    passed[i] = converter.run(args[i]) // 转换实参
                } else {
                    passed[i] = null
                }
            }
            i++
        }
        try {
            // rpc调用并转换返回值
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