package net.jkcode.jksoa.client.connection

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jksoa.client.IRpcClient
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 自动重连的连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 12:48 PM
 */
class ReconnectableConnection private constructor(url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

        /**
         * 单例池
         */
        protected var conns: ConcurrentHashMap<IUrl, ReconnectableConnection> = ConcurrentHashMap();

        /**
         * 获得单例
         */
        public fun instance(url: Url): ReconnectableConnection {
            return conns.getOrPutOnce(url) {
                ReconnectableConnection(url)
            }
        }
    }

    /**
     * 被代理的连接
     */
    protected var conn: BaseConnection? = null

    /**
     * 引用数
     *   被 ReusableConnection 引用时: 引用数++
     *   关闭时: 引用数--
     */
    protected val refNum: AtomicInteger = AtomicInteger(0)

    /**
     * 上一次发送的时间
     */
    public var lastSendTime: Long = 0
        protected set

    init {
        if(config["lazyConnect"]!!) // 延迟创建连接
            getOrReConnect()
    }

    /**
     * 增加引用数
     *   关闭时: 引用数--
     * @return
     */
    public fun incrRef(): ReconnectableConnection {
        refNum.incrementAndGet()
        return this
    }

    /**
     * 获得连接或重新连接
     * @return
     */
    protected fun getOrReConnect(): BaseConnection {
        if(conn == null){
            synchronized(this){
                if(conn == null) {
                    // 根据rpc协议获得对应的client
                    val client = IRpcClient.instance(url.protocol)
                    // 连接server
                    conn = client.connect(url) as BaseConnection
                    // 连接关闭回调
                    conn!!.closeCallback = {
                        onConnectionClosed()
                    }
                }
            }
        }
        return conn!!
    }

    /**
     * 客户端发送请求
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        lastSendTime = currMillis()

        // 发送请求
        return getOrReConnect().send(req, requestTimeoutMillis)
    }

    /**
     * 处理连接被关闭事件
     *   由netty被动触发
     */
    protected fun onConnectionClosed() {
        // 1 调用回调
        closeCallback?.invoke(conn!!)
        // 2 清空被代理的连接
        conn = null
    }

    /**
     * 关闭连接
     *   系统主动触发
     */
    public override fun close() {
        // 引用数--, 减至0才关闭
        if(refNum.decrementAndGet() > 0)
            return

        // 真正的关闭
        if(conn != null) {
            conn!!.close()
            conn = null
            refNum.set(0)
        }
    }

}