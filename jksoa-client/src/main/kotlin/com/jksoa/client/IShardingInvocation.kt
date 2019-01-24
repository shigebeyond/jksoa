package com.jksoa.client

import com.jksoa.common.IInvocationMethod

/**
 * 分片方法调用的描述
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IShardingInvocation: IInvocationMethod {

    /**
     * 分片总数
     */
    val shardingSize: Int
        get() = shardingArgses.size

    /**
     * 分片要调用的实参
     */
    val shardingArgses: Array<Array<*>>
}