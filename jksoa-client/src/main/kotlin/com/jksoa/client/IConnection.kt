package com.jksoa.client

import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.future.IRpcResponseFuture
import java.io.Closeable

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
interface IConnection: Closeable {

    /**
     * 服务端地址
     */
    val url: Url

    /**
     * 权重
     */
    var weight: Int

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    fun send(req: IRpcRequest): IRpcResponseFuture
}