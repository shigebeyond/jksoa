package net.jkcode.jksoa.rpc.client.jphp

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkutil.common.getClassByName
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jkutil.common.substringBetween
import net.jkcode.jkutil.fiber.AsyncCompletionStage
import net.jkcode.jphp.ext.PhpMethod
import net.jkcode.jphp.ext.WrapCompletableFuture
import php.runtime.Memory
import php.runtime.env.Environment
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.memory.ObjectMemory
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.MethodEntity
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * 包装远程方法
 *   负责参数类型+返回值类型转换(参考 JavaObject 实现)
 *   被 PhpReferer 缓存+引用
 */
class PhpRefererMethod(public val phpRef: PhpReferer, public val phpMethod: MethodEntity) {

    constructor(phpRef: PhpReferer, phpMethod: String): this(phpRef, phpRef.phpClass.findMethod(phpMethod.toLowerCase()))

    // ---- java 方法 ----
    public lateinit var methodSignature: String // java远程方法
    public var paramTypes: Array<Class<*>> = emptyArray() // 参数类型
    public var converters: Array<MemoryUtils.Converter<*>> = emptyArray() // 参数转换器
    public var returnType: Class<*> = Void.TYPE // 返回值类型
    public var resultConverter: MemoryUtils.Converter<*>? = null // 返回值转换器

    init {
        /* 直接调用映射的php方法，结果即为注解+java方法签名(带返回值类)
        function getUserByNameAsync($name){
            return 'java.util.concurrent.CompletableFuture getUserByNameAsync(java.lang.String)';
        } */
        val code = phpMethod.invokeStatic(phpRef.env).toString()
        parseJavaMethod(code)
    }

    /**
     * 解析java方法
     * @param line java方法声明，组成:`返回值+方法名+参数类`
     *      如 java.util.concurrent.CompletableFuture getUserByNameAsync(java.lang.String)
     */
    protected fun parseJavaMethod(line: String) {
        val parts = line.split(' ', limit = 2)
        methodSignature = parts[1]
        // 解析参数类型+返回值类型 TODO: 未处理泛型
        // 获得参数片段：括号包住
        val paramString = methodSignature.substringBetween('(', ')')
        if (paramString.isNotBlank()) {
            // 解析参数类型
            paramTypes = paramString.split(",\\s*").map {
                val clazzName = it.trim()
                getClassByName(clazzName)
            }.toTypedArray()
            converters = MemoryUtils.getConverters(paramTypes)
        }
        // 获得返回值类型：空格之前
        val returnString = parts[0].trim()
        if (returnString.isNotBlank()) {
            returnType = getClassByName(returnString)
            resultConverter = MemoryUtils.getConverter(returnType)
        }
    }

    /**
     * php调用实现
     *    先转实参，再发rpc请求， 后转返回值
     * @param env
     * @param args 方法实参，不包含this或方法名， 因为不需要
     */
    @Suspendable
    public fun phpInvoke(env: Environment, vararg args: Memory): Memory {
        // 1 转换实参
        val passed = convertArguments(args, env)

        // 2 发送rpc请求
        val ret = javaInvoke(env, passed)

        // 3 转换返回值
        return convertReturnValue(ret, env)
    }

    /**
     * java的调用实现
     *    将方法调用转为发送rpc请求
     * @param env
     * @param args 参数
     * @return
     */
    @Suspendable
    public fun javaInvoke(env: Environment, args: Array<Any?>): Any? {
        // 1 封装请求
        val req = RpcRequest(phpRef.serviceId, methodSignature, args)

        // 2 分发请求, 获得异步响应
        val resFuture = RpcInvocationHandler.invoke(req)
        return getResultFromFuture(resFuture)
    }

    /**
     * 转换实参
     */
    public fun convertArguments(args: Array<out Memory>, env: Environment): Array<Any?> {
        val len = args.size
        if (len < paramTypes.size || len > paramTypes.size)
            JavaReflection.exception(env, IllegalArgumentException("Invalid argument count"))
        // 转换参数
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
        return passed
    }

    /**
     * 转换返回值
     */
    public fun convertReturnValue(result: Any?, env: Environment): Memory {
        if(result == null)
            return Memory.NULL

        if (resultConverter != null)
            return MemoryUtils.valueOf(result)

        return when (returnType) {
            Void.TYPE -> Memory.NULL
            CompletableFuture::class.java -> ObjectMemory(WrapCompletableFuture(env, result as CompletableFuture<*>))
            else -> ObjectMemory(JavaObject.of(env, result))
        }
        return Memory.NULL
    }

    /**
     * 从CompletableFuture获得方法结果值
     *
     * @param resFuture
     * @return
     */
    @Suspendable
    fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?{
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