package net.jkcode.jksoa.common

import net.jkcode.jkutil.common.getSignature
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.annotation.remoteService
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 分片的rpc请求
 *    远端方法调用的描述: 方法 + 参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
class ShardingRpcRequest(clazz: String, //服务接口类全名
                         methodSignature: String, //要调用的方法签名：包含方法名+参数类型
                         args: Array<Any?> , //实参
                         public override val argsPerSharding: Int, // 每个分片的参数个数
                         version: Int = 0 // 版本
) : IShardingRpcRequest, RpcRequest(clazz, methodSignature, args, version) {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?>, argsPerSharding: Int) : this(method.getServiceClass().name, method.getSignature(), args, argsPerSharding, method.getServiceClass().remoteService?.version ?: 0)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?>, argsPerSharding: Int) : this(func.javaMethod!!, args, argsPerSharding)

    public override fun toString(): String {
        return "ShardingRpcRequest: " + toDesc()
    }

    /**
     * 构建rpc请求
     * @param 分片序号
     * @return
     */
    public override fun buildRpcRequest(iSharding: Int): IRpcRequest {
        val req = RpcRequest(serviceId, methodSignature, getShardingArgs(iSharding), version)
        if(attachments != null)
            req.putAttachments(attachments!!)
        return req
    }

}