package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config

/**
 * 默认的请求超时
 */
public val DefaultRequestTimeoutMillis:Long = Config.instance("client", "yaml")["requestTimeoutMillis"]!!

/**
 * rpc调用的元数据
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 4:03 PM
 */
interface IRpcRequestMeta {

    /**
     * 版本
     */
    val version: Int


    /**
     * 请求超时
     */
    val requestTimeoutMillis: Long
}