package com.jksoa.client.connection

import com.jksoa.client.IConnection
import com.jksoa.common.*

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
abstract class BaseConnection(public override val url: Url /* 服务端地址 */,
                              public override var weight: Int = 1 /* 权重 */
) : IConnection {

    /**
     * 连接关闭的回调
     */
    internal var closeCallback: ((BaseConnection) -> Unit)? = null
}