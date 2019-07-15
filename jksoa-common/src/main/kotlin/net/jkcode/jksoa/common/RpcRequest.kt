package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.annotation.remoteService
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
data class RpcRequest(public override val clazz: String, /* 服务接口类全名 */
                      public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                      public override val args: Array<Any?> = emptyArray() /* 实参 */,
                      public override val version: Int = 0 /* 版本 */
): IRpcRequest {

    companion object {

        /**
         * 线程安全的server端请求对象缓存
         */
        protected val reqs:ThreadLocal<RpcRequest> = ThreadLocal();

        /**
         * 设置server端当前请求
         *   在server端的 RpcRequestHandler 中创建 RpcContext 时调用
         */
        public fun setCurrent(req: RpcRequest){
            reqs.set(req)
        }

        /**
         * 获得server端当前请求
         */
        @JvmStatic
        public fun current(): RpcRequest {
            return reqs.get()!!;
        }

        /**
         * 是否服务端
         *   至于是不是客户端或其他端, 老子不管
         * @return
         */
        public val isServerSide: Boolean
            get() = reqs.get() != null
    }

    /**
     * 请求标识，全局唯一
     */
    public override val id: Long = generateId("rpc")

    /**
     * 附加参数
     */
    public override val attachments: MutableMap<String, Any?> = LazyAllocatedMap()

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray()) : this(method.getServiceClass().name, method.getSignature(), args, method.getServiceClass().remoteService?.version ?: 0)

    /**
     * 构造函数
     *   如果被调用的kotlin方法中有默认参数, 则 func.javaMethod 获得的java方法签名是包含默认参数类型的
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "RpcRequest: " + toDesc()
    }

}