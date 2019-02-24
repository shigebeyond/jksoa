package com.jksoa.common

/**
 * rpc调用的元数据
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 4:03 PM
 */
interface IRpcInvocationMeta {

    /**
     * 版本
     */
    val version: Int


    /**
     * 请求超时
     */
    val requestTimeoutMillis: Long
}