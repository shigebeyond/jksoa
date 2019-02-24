package com.jksoa.common

import com.jksoa.common.invocation.IInvocation
import java.io.Serializable

/**
 * rpc请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IRpcRequest: Serializable, IInvocation, IRpcRequestMeta {

    /**
     * 服务标识，即接口类全名
     */
    val serviceId: String
        get() = clazz

}