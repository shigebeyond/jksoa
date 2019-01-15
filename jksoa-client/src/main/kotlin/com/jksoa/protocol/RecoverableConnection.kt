package com.jksoa.protocol

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.future.IRpcResponseFuture
import com.jksoa.protocol.netty.NettyConnection
import io.netty.util.AttributeKey

/**
 * 可恢复的连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 12:48 PM
 */
class RecoverableConnection(url: Url, weight: Int = 1) : IConnection(url, weight) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

        /**
         * 自动重连的时间阀值, 在这段时间内连接断开, 则自动重连
         */
        public val autoReconnectTimeSeconds: Int = config["autoReconnectTimeSeconds"]!!
    }

    /**
     * 被代理的连接
     */
    protected var conn: IConnection? =
            if(config["lazyConnection"]!!) // 延迟创建连接
                null
            else
                newConnection()

    /**
     * 根据url建立新连接
     *
     * @return
     */
    protected fun newConnection(): IConnection {
        // 根据rpc协议获得对应的client
        val client = IProtocolClient.instance(url.protocol)
        // 连接server
        val conn = client.connect(url)
        // 连接关闭回调
        conn.closeCallback = {
            onConnectionClosed()
        }
        return conn
    }

    /**
     * 处理连接被关闭事件
     */
    protected fun onConnectionClosed() {
        // 1 调用回调
        closeCallback?.invoke()
        // 2 清空被代理的连接
        conn = null
        // 3 自动重连: 在时间阀值内连接断开, 则自动重连
        if(System.currentTimeMillis() < lastSendTime + autoReconnectTimeSeconds * 1000)
            conn = newConnection()
    }

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun doSend(req: IRpcRequest): IRpcResponseFuture {
        // 建立连接
        if(conn == null)
            synchronized(this) {
                if(conn == null)
                    conn = newConnection()
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