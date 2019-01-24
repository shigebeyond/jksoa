package com.jksoa.common

import com.jkmvc.common.getSignature
import com.jkmvc.common.isSubClass
import com.jkmvc.idworker.IIdWorker
import com.jksoa.common.exception.RpcClientException
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
class RpcRequest(public override val clazz: String, /* 服务接口类全名 */
                 public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                 public override val args: Array<Any?> = emptyArray(), /* 实参 */
                 public override val id: Long = idWorker.nextId() /* 请求标识，全局唯一 */
): IRpcRequest {

    companion object {

        /**
         * id生成器
         */
        protected val idWorker: IIdWorker = IIdWorker.instance("snowflakeId")

        /**
         * 线程安全的请求对象缓存
         */
        protected val reqs:ThreadLocal<RpcRequest> = ThreadLocal();

        /**
         * 获得当前请求
         */
        @JvmStatic
        public fun current(): RpcRequest {
            return reqs.get()!!;
        }
    }

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray(), id: Long = idWorker.nextId()) : this(method.getServiceClass().name, method.getSignature(), args, id)

    /**
     * 构造函数
     *   如果被调用的kotlin方法中有默认参数, 则 func.javaMethod 获得的java方法签名是包含默认参数类型的
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    init{
        reqs.set(this);
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "RpcRequest: id=$id, service=$serviceId.$methodSignature, args=" + args.joinToString(", ", "[", "]");
    }

}