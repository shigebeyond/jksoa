package com.jksoa.protocol

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRpcRequest
import com.jksoa.common.IUrl
import com.jksoa.common.Url
import com.jksoa.common.future.IRpcResponseFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 可恢复的连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 12:48 PM
 */
class RecoverableConnection(url: Url, weight: Int = 1) : BasicConnection(url, weight) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

        /**
         * 单例池
         */
        protected var conns: ConcurrentHashMap<IUrl, RecoverableConnection> = ConcurrentHashMap();

        /**
         * 获得单例
         */
        public fun instance(url: Url): RecoverableConnection {
            return conns.getOrPut(url){
                RecoverableConnection(url)
            }
        }
    }

    /**
     * 被代理的连接
     */
    protected var conn: BasicConnection? =
            if(config["lazyConnection"]!!) // 延迟创建连接
                null
            else
                newConnection()

    /**
     * 上一次发送的时间
     */
    public var lastSendTime: Long = 0
        protected set

    /**
     * 根据url建立新连接
     *
     * @return
     */
    protected fun newConnection(): BasicConnection {
        // 根据rpc协议获得对应的client
        val client = IProtocolClient.instance(url.protocol)
        // 连接server
        val conn = client.connect(url) as BasicConnection
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
        closeCallback?.invoke(conn!!)
        // 2 清空被代理的连接
        conn = null
        // 3 自动重连: 在时间阀值内连接断开, 则自动重连
        val autoReconnectTimeSeconds: Int = config["autoReconnectTimeSeconds"]!! // 自动重连的时间阀值
        if(System.currentTimeMillis() < lastSendTime + autoReconnectTimeSeconds * 1000)
            conn = newConnection()
    }

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRpcRequest): IRpcResponseFuture {
        lastSendTime = System.currentTimeMillis()

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