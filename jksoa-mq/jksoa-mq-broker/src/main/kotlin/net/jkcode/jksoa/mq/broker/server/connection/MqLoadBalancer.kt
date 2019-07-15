package net.jkcode.jksoa.mq.broker.server.connection

import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.loadbalance.ILoadBalancer
import net.jkcode.jksoa.mq.common.Message

/**
 * 消息的均衡负载算法
 *   1 无序消息: 随机选择 2 有序消息: 一致性哈希
 *   仅用在 ConsumerConnectionHub
 *
 * @author shijianhang
 * @create 2019-02-18 下午9:21
 **/
internal class MqLoadBalancer : ILoadBalancer {

    /**
     * 选择连接
     *
     * @param conn
     * @param req
     * @return
     */
    public override fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection? {
        if(conns.isEmpty())
            return null

        // 消息
        val msg = req.args.first() as Message

        // 1 无序: 随机选个连接
        if(msg.subjectId == 0L) {
            val i = randomInt(conns.size)
            return conns[i]
        }

        // 2 有序: 一致性哈希
        return ConsistentHash(3, 100, conns).get(msg.subjectId)
    }
}