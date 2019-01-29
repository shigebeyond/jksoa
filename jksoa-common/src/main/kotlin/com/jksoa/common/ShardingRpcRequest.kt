package com.jksoa.common

import com.jkmvc.common.getSignature
import com.jkmvc.common.generateId
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 分片的rpc请求
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
class ShardingRpcRequest(override val clazz: String, /* 服务接口类全名 */
                         override val methodSignature: String, /* 要调用的方法签名：包含方法名+参数类型 */
                         override val shardingArgses: Array<Array<*>> /* 分片要调用的实参 */
) : IShardingRpcRequest {

    /**
     * 请求标识，全局唯一
     */
    public override val id: Long = generateId()

    /**
     * 构造函数
     *
     * @param intf 接口类
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     */
    protected constructor(intf: Class<out IService>, method: Method, shardingArgses: Array<Array<*>>): this(intf.name, method.getSignature(), shardingArgses)

    /**
     * 构造函数
     *
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(method: Method, shardingArgses: Array<Array<*>>) : this(method.getServiceClass(), method, shardingArgses)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(func: KFunction<*>, shardingArgses: Array<Array<*>>) : this(func.javaMethod!!, shardingArgses)

    public override fun toString(): String {
        return "ShardingRpcRequest: " + toDesc()
    }
}