package net.jkcode.jksoa.rpc.client.k8s

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.connection.single.ReconnectableConnection
import net.jkcode.jksoa.rpc.example.ISimpleService

/**
 * k8s模式下的连接
 *   1 继承ReconnectableConnection，支持延迟连接 + 自动重连，见getOrReConnect()
 *   2 获得并记录server端的容器id
 *   2.1 通过rpc (ISimpleService::hostname) 来获得
 *   2.2 由于是延迟连接，因此不能在构造函数时就获得，只能在有真正连接(如发送请求)时才获得
 *   2.3 延迟获得的时机与调用栈
 *       K8sConnectionHub.select() -> toDesc() -> lazy属性serverId读 -> rpc获得server端的容器id
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2023-7-24 3:18 PM
 */
class K8sConnection(url: Url) : ReconnectableConnection(url) {

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
    public val serverId: String? by lazy{
        //getOrReConnect().serverIp // service vip
        if (isValid())
            requestServiceId()
        else
            null
    }

    /**
     * rpc请求server hostname(容器id)
     */
    private fun requestServiceId(): String {
        // rpc请求server hostname(容器id)
        val req = RpcRequest(ISimpleService::hostname)
        return send(req, 500).get().getOrThrow() as String
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
        return "K8sConnection(serverId=" + serverId + ", url=" + url + ')'
    }

}