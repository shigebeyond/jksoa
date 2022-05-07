package net.jkcode.jksoa.rpc.client.jphp

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkutil.common.getClassByName
import net.jkcode.jkutil.common.substringBetween
import net.jkcode.jkutil.fiber.AsyncCompletionStage
import net.jkcode.jphp.ext.WrapCompletableFuture
import php.runtime.Memory
import php.runtime.env.Environment
import php.runtime.ext.java.JavaObject
import php.runtime.ext.java.JavaReflection
import php.runtime.memory.ObjectMemory
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.ClassEntity
import php.runtime.reflection.MethodEntity
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * 包装远程方法
 *   1. 负责将 PhpReferer 的php方法调用转为发java rpc请求
 *   2. php映射方法实现直接返回java方法签名，用作php与java之间的调用映射
 *   3. 调用时参数/返回值类型转换：负责根据java方法签名， 来转换参数/返回值类型(参考 JavaObject 实现)
 *   4. 不包含的降级的本地方法
 *   5. 被 PhpReferer 缓存+引用
 */
class PhpRefererMethod(
        public val phpRef: PhpReferer,
        public val phpMethod: MethodEntity // 映射方法
) {

    public val clazz: ClassEntity
        get() = phpMethod.clazz

    // ---- java 方法 ----
    public lateinit var methodSignature: String // java远程方法
    public var paramTypes: Array<Class<*>> = emptyArray() // 参数类型
    public var converters: Array<MemoryUtils.Converter<*>> = emptyArray() // 参数转换器
    public var returnType: Class<*> = Void.TYPE // 返回值类型
    public var resultUnconverter: MemoryUtils.Unconverter<*>? = null // 返回值转换器

    init {
        /* 直接调用映射的php方法，结果即为java方法签名(带返回值类)
        function getUserByNameAsync($name){
            return 'java.util.concurrent.CompletableFuture getUserByNameAsync(java.lang.String)';
        } */
        val methodSign = phpMethod.invokeStatic(phpRef.env).toString()
        parseJavaMethod(methodSign)
    }

    /**
     * 解析java方法
     * @param methodSign java方法声明，组成:`返回值+方法名+参数类`
     *      如 java.util.concurrent.CompletableFuture getUserByNameAsync(java.lang.String)
     */
    protected fun parseJavaMethod(methodSign: String) {
        val parts = methodSign.split(' ', limit = 2)
        methodSignature = parts[1]
        // 解析参数类型+返回值类型
        // 1 获得参数片段：括号包住
        val paramString = methodSignature.substringBetween('(', ')')
        if (paramString.isNotBlank()) {
            // 解析参数类型
            paramTypes = paramString.split(",\\s*").map {
                val clazzName = it.trim()
                getClassByName(clazzName) // 去掉泛型了
            }.toTypedArray()
            converters = MemoryUtils.getConverters(paramTypes)
        }
        // 2 获得返回值类型：空格之前
        val returnString = parts[0].trim()
        if (returnString.isNotBlank()) {
            returnType = getClassByName(returnString)
            resultUnconverter = MemoryUtils.getUnconverter(returnType)
        }
    }

    /**
     * 包含rpc调用
     *    先转实参，再发rpc调用，后转返回值
     *    由于无需调用invoke(), 因此不实现invoke(), 只实现wrapInvoke(), 防止写多了难理解
     * @param env
     * @param args 方法实参，不包含this或方法名
     * @param rpcInvoker rpc调用, 一般是 MethodGuardInvoker.guardInvoke
     * @return
     */
    @Suspendable
    public fun wrapInvoke(env: Environment, args: Array<out Memory>, rpcInvoker: (Array<Any?>)->Any?): Memory {
        // 1 转换实参
        val passed = convertArguments(args, env)

        // 2 发送rpc请求
        val ret = rpcInvoker(passed)

        // 3 转换返回值
        return convertReturnValue(ret, env)
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

        if (resultUnconverter != null)
            return MemoryUtils.valueOf(result)

        return when (returnType) {
            Void.TYPE -> Memory.NULL
            else -> ObjectMemory(JavaObject.of(env, result))
        }
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