package com.jksoa.job

import com.jksoa.common.RpcRequest

/**
 * 作业信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
interface IJob {

    /**
     * 要调用的服务标识，即接口类全名
     */
    val serviceId: String

    /**
     * 要调用的方法签名：包含方法名+参数类型
     */
    val methodSignature: String

    /**
     * 分片总数
     */
    val shardingNum: Int

    /**
     * 某个分片要调用的参数工厂
     */
    val shardingParamsFactory: (Int) -> Array<Any?>

    /**
     * 构建指定分片的rpc请求
     * @param iSharding
     * @return
     */
    fun buildShardingRpcRequest(iSharding: Int): RpcRequest
}