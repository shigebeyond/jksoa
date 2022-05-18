package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.Url
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
class SwarmConnection(url: Url): ReconnectableConnection(url) {

    /**
     * 服务器id=容器id
     */
    public val serverId: String?
        get(){
            if(_serverId == null)
                _serverId = requestServiceId()
            return _serverId
        }

    /**
     * 服务器id=容器id
     */
    protected var _serverId: String? = null

    /**
     * rpc请求server hostname(容器id)
     *    有连接才请求，否则不请求，不会为了获得server id而随意新建连接，本来就是server id就是更有效的利用连接的
     * @return
     */
    private fun requestServiceId(): String? {
        // 有效连接才rpc
        if(!isValid())
            return null

        // rpc请求server hostname(容器id)
        val req = RpcRequest(ISimpleService::hostname)
        val res = conn!!.send(req, 500)
        return res.get().getOrThrow() as String
    }

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
        return "SwarmConnection(serverId=" + serverId + ", url=" + url + ')'
    }

}