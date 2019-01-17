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
interface IConnection: Closeable, INode {

    /**
     * 服务端地址
     */
    val url: Url

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    fun send(req: IRpcRequest): IRpcResponseFuture
}