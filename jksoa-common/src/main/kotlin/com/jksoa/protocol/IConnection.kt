package com.jksoa.protocol

import com.jksoa.common.IUrl
import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.Url
import java.io.Closeable

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
abstract class IConnection(public val url: Url /* 服务端地址 */): Closeable, IUrl by url {

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public abstract fun send(req: Request): Response
}