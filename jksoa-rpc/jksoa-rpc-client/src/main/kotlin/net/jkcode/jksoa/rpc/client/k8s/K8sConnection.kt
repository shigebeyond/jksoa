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
     *    仅用于负载均衡，仅强制+有连接时才会rpc请求，否则不请求，因为一旦请求了没连接就会新建连接
     * @param force 是否强制查询(rpc), 仅测试用
     * @return
     */
    public fun getServerId(force: Boolean = false): String? {
        // 1 service vip
        //return getOrReConnect().serverIp

        // 2 pod ip
        // 若连接无效，则_serverId也无效
        if(!isValid())
            _serverId = null
        // 若_serverId无效，则请求
        if (_serverId == null){
            // 强制或有效连接才rpc请求，空连接就不请求了，因为一旦请求了没连接就会新建连接: 不会为了获得serverId而随意新建连接，本来serverId就是为了更有效的利用连接的
            if (force || isValid())
                synchronized(this){
                    if(_serverId == null) // 双重检查
                        _serverId = requestServiceId() // 请求后会记录 _serverId
                }
        }
        return _serverId
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
    public override fun toString(): String {
        return "K8sConnection(serverId=" + getServerId() + ", url=" + url + ')'
    }

}