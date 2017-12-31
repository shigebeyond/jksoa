package com.jksoa.common

import java.io.Serializable

/**
 * rpc请求
 *
 * @ClassName: Request
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IRequest: Serializable {

    /**
     * 服务标识，即接口类全名
     */
    val serviceId: String

    /**
     * 方法签名：包含方法名+参数类型
     */
    val methodSignature: String

    /**
     * 实参
     */
    val args: Array<Any>

    /**
     * 请求标识，全局唯一
     */
    val id: Long

}