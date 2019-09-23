package net.jkcode.jksoa.rpc.client.connection

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.*

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
abstract class BaseConnection(public override val url: Url /* 服务端地址 */,
                              public override var weight: Int = 1 /* 权重 */
) : IConnection {

    /**
     * 是否有效连接
     * @return
     */
    public override fun isValid(): Boolean{
        return true;
    }

    /**
     * 改写 hashCode(), 用在 ConsistentHash 计算哈希
     */
    public override fun hashCode(): Int {
        return url.hashCode()
    }

    /**
     * 改写 toString()
     */
    public override fun toString(): String {
        return this::class.qualifiedName + '(' + url + ')'
    }
}