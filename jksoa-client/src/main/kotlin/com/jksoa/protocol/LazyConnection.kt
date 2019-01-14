package com.jksoa.protocol

import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.future.IRpcResponseFuture

/**
 * 延迟的连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 12:48 PM
 */
class LazyConnection(url: Url, weight: Int = 1) : IConnection(url, weight) {

    /**
     * 被代理的连接
     */
    protected var conn: IConnection? = null

    /**
     * 根据url建立连接
     *
     * @return
     */
    protected fun buildConnection(): IConnection {
        // 根据rpc协议获得对应的client
        val client = IProtocolClient.instance(url.protocol)
        // 连接server
        val conn = client.connect(url)
        // 连接关闭回调
        conn.closeCallback = {
            // 代理回调的调用
            this.closeCallback?.invoke()
            // 清空被代理的连接
            this.conn = null
        }
        return conn
    }

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRpcRequest): IRpcResponseFuture {
        // 建立连接
        if(conn == null)
            synchronized(this) {
                if(conn == null)
                    conn = buildConnection()
            }

        // 发送请求
        return conn!!.send(req)
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        if(conn != null) {
            conn!!.close()
            conn = null
        }
    }

}