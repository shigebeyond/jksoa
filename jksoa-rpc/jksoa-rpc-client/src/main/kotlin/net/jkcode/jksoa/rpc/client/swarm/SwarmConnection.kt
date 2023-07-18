package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.connection.single.ReconnectableConnection
import net.jkcode.jksoa.rpc.example.ISimpleService

/**
 * docker swarm模式下的连接
 *   1 继承ReconnectableConnection，支持延迟连接 + 自动重连，见getOrReConnect()
 *   2 获得并记录server端的容器id
 *   2.1 通过rpc (ISimpleService::hostname) 来获得
 *   2.2 由于是延迟连接，因此不能在构造函数时就获得，只能在有真正连接(如发送请求)时才获得
 *   2.3 延迟获得的时机与调用栈
 *       SwarmConnectionHub.select() -> toDesc() -> lazy属性serverId读 -> rpc获得server端的容器id
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class SwarmConnection(url: Url) : ReconnectableConnection(url) {

    /**
     * 服务器id=容器id
     */
    @Volatile
    protected var _serverId: String? = null

    /**
     * 获得服务器id=pod ip
     * @param force 是否强制查询(rpc)，仅在测试时才为true
     * @return
     */
    public fun getServerId(force: Boolean = false): String? {
        return getOrReConnect().serverIp // pod ip

        // 若_serverId无效，则请求
        /*if (_serverId == null || !isValid() *//* 若连接无效，则_serverId也无效 *//*){
            // 强制或有效连接 才rpc请求, 否则不请求: 不会为了获得serverId而随意新建连接，本来serverId就是为了更有效的利用连接的
            if (force || isValid()) {
                synchronized(this){
                    if(_serverId == null) // 双重检查
                        requestServiceId() // 请求后会记录 _serverId
                }
            }else{
                _serverId = null
            }
        }
        return _serverId*/
    }

    /**
     * rpc请求server hostname(容器id)
     *   请求后会记录 _serverId，见 send()
     */
    private fun requestServiceId() {
        // rpc请求server hostname(容器id)
        val req = RpcRequest(ISimpleService::hostname)
        val res = send(req, 500)
        // 请求后会记录 _serverId，见 send()
        res.get()
    }

    /**
     * 改写send()
     *   如果是 ISimpleService::hostname 请求，则会记录 _serverId，用以复用 hostname 请求结果，如测试时client直接rpc ISimpleService::hostname 请求即能同步更新 serverId，以便节省一次请求
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    /*override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        val ret = super.send(req, requestTimeoutMillis)
        // 对 ISimpleService::hostname rpc请求，要记录 _serverId
        if (req.methodSignature == "hostname()" && req.clazz == ISimpleService::class.qualifiedName)
            ret.thenAccept {
                // 收到响应时会记录 _serverId
                _serverId = it.getOrThrow() as String
            }
        return ret
    }*/

    /**
     * 处理重连事件： 触发刷新服务器id
     */
    override fun onReConnect(conn: BaseConnection) {
        _serverId = null
    }

    /**
     * 转描述
     */
    public fun toDesc(): String {
        return "SwarmConnection(serverId=" + getServerId() + ", url=" + url + ')'
    }

}