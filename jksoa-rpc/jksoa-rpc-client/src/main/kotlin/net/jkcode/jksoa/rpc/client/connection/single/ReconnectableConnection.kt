package net.jkcode.jksoa.rpc.client.connection.single

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.getOrPutOnce
import net.jkcode.jkutil.common.currMillis
import net.jkcode.jksoa.rpc.client.IRpcClient
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
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
class ReconnectableConnection internal constructor(url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

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
     *   被 SingleConnection 引用时: 引用数++
     *   关闭时: 引用数--
     */
    protected val refs: AtomicInteger = AtomicInteger(0)

    /**
     * 上一次发送的时间
     */
    public var lastSendTime: Long = 0
        protected set

    init {
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) // 不延迟创建连接: 预先创建
            getOrReConnect()
    }

    /**
     * 增加引用数
     *   关闭时: 引用数--
     * @return
     */
    public fun incrRef(): ReconnectableConnection {
        refs.incrementAndGet()
        return this
    }

    /**
     * 获得连接或重新连接
     * @return
     */
    protected fun getOrReConnect(): BaseConnection {
        // 1 有连接: 检查是否有效
        if(conn != null){
            // 1.1 有效连接
            if(conn!!.isValid())
                return conn!!

            // 1.2 无效连接: 关闭连接
            clientLogger.debug("关闭无效连接: {}", conn)
            synchronized(this) {
                if(conn != null) {
                    conn!!.close()
                    conn = null
                }
            }
        }

        // 2 无连接: 创建新连接
        synchronized(this){
            if(conn == null) {
                // 根据rpc协议获得对应的client
                val client = IRpcClient.instance(url.protocol)
                // 连接server
                conn = client.connect(url) as BaseConnection
            }
        }
        clientLogger.debug("创建新连接: {}", conn)
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
     * 关闭连接
     *   系统主动触发
     */
    public override fun close() {
        // 引用数--, 减至0才关闭
        if(refs.decrementAndGet() > 0)
            return

        // 真正的关闭
        if(conn != null) {
            conn!!.close()
            conn = null
            refs.set(0)
        }
    }

}