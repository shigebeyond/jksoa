package com.jksoa.protocol

import com.jksoa.common.*
import com.jksoa.common.future.IRpcResponseFuture
import com.jksoa.loadbalance.INode
import java.io.Closeable

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
abstract class IConnection(public val url: Url /* 服务端地址 */,
                           public override var weight: Int = 1 /* 权重 */
) : Closeable, INode {

    /**
     * 连接是否关闭
     */
    public abstract val closed: Boolean

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public abstract fun send(req: IRpcRequest): IRpcResponseFuture
}