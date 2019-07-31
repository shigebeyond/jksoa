package net.jkcode.jksoa.mq.connection

import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jksoa.rpc.client.IConnection

/**
 * broker端的consumer连接的列表 + 一致性hash的处理
 *    只在 ConsumerConnectionHub 中调用, 只改写 add() / remove() 方法, 因为 ConsumerConnectionHub 中就调用了这2个方法
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-31 9:06 PM
 */
internal class ConsumerConnectionList(protected val list: MutableList<IConnection> = ArrayList()) : MutableList<IConnection> by list {

    /**
     * 一致性hash
     */
    public val consistentHash = ConsistentHash(3, 100, list)

    public override fun add(conn: IConnection): Boolean {
        consistentHash.add(conn)
        return list.add(conn)
    }

    public override fun remove(conn: IConnection): Boolean {
        consistentHash.remove(conn)
        return list.remove(conn)
    }


}