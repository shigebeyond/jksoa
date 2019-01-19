package com.jksoa.common

import com.jkmvc.common.getSignature
import com.jkmvc.idworker.IIdWorker
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
class RpcRequest(public override val serviceId: String, /* 服务标识，即接口类全名 */
              public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
              public override val args: Array<Any?> = emptyArray(), /* 实参 */
              public override val id: Long = idWorker.nextId() /* 请求标识，全局唯一 */
): IRpcRequest {

    companion object {

        /**
         * id生成器
         */
        protected val idWorker: IIdWorker = IIdWorker.instance("snowflakeId")
    }

    /**
     * 构造函数
     *
     * @param intf 接口类
     * @param method 方法
     * @param args 实参
     * @param id 请求标识
     */
    public constructor(intf: Class<out IService>, method: Method, args: Array<Any?> = emptyArray(), id: Long = idWorker.nextId()): this(intf.name, method.getSignature(), args, id){}

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray(), id: Long = idWorker.nextId()) : this(method.getServiceClass(), method, args, id)

    /**
     * 构造函数
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
        return "id=$id, service=$serviceId.$methodSignature, args=" + args.joinToString(", ", "[", "]");
    }

}