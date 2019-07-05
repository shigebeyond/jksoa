package net.jkcode.jksoa.client.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.common.*

/**
 * 可复用的rpc连接
 *   复用的是同一个server的连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
class ReusableConnection(public override val url: Url /* 服务端地址 */,
                         public override var weight: Int = 1 /* 权重 */
) : IConnection by ReconnectableConnection.instance(url.serverPart).incrRef() // 根据 serverPart 来复用 ReconnectableConnection 的实例
{
    /**
     * 改写 hashCode(), 用在 ConsistentHash 计算哈希
     */
    public override fun hashCode(): Int {
        return url.hashCode()
    }

}