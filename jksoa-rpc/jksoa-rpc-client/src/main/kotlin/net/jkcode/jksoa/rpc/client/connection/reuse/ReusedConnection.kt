package net.jkcode.jksoa.rpc.client.connection.reuse

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.*

/**
 * 可复用的rpc连接
 *   根据 serverPart 来复用 ReconnectableConnection 的实例
 *   复用的是同一个server的连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
class ReusedConnection(
        public override val url: Url, // 服务端地址
        public override var weight: Int = 1, // 权重
        protected val conn: IConnection = ReconnectableConnection.instance(url.serverPart).incrRef() // 根据 serverPart 来复用 ReconnectableConnection 的实例
) : IConnection by conn
{
}