package com.jksoa.common

import com.jkmvc.common.getSignature
import com.jkmvc.idworker.IIdWorker
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
                         override val shardingArgses: Array<Array<*>>, /* 分片要调用的实参 */
                         public override val id: Long = idWorker.nextId() /* 请求标识，全局唯一 */
) : IShardingRpcRequest {

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
     * @param shardingArgses 分片要调用的实参
     * @param id 请求标识，全局唯一
     */
    protected constructor(intf: Class<out IService>, method: Method, shardingArgses: Array<Array<*>>, id: Long = idWorker.nextId()): this(intf.name, method.getSignature(), shardingArgses, id)

    /**
     * 构造函数
     *
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     * @param id 请求标识，全局唯一
     */
    public constructor(method: Method, shardingArgses: Array<Array<*>>, id: Long = idWorker.nextId()) : this(method.getServiceClass(), method, shardingArgses, id)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param shardingArgses 分片要调用的实参
     * @param id 请求标识，全局唯一
     */
    public constructor(func: KFunction<*>, shardingArgses: Array<Array<*>>, id: Long = idWorker.nextId()) : this(func.javaMethod!!, shardingArgses, id)

    public override fun toString(): String {
        return "ShardingRpcRequest: " + toDesc()
    }
}