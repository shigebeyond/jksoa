package net.jkcode.jksoa.mq.broker.server.connection

import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.loadbalance.ILoadBalancer
import net.jkcode.jksoa.mq.common.Message

/**
 * 消息的均衡负载算法
 *   1 无序消息: 随机选择 2 有序消息: 固定选择
 *
 * @author shijianhang
 * @create 2019-02-18 下午9:21
 **/
class MqLoadBalancer : ILoadBalancer {

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

        var i: Int
        if(msg.subjectId == 0L) // 无序: 随机选个连接
            i = randomInt(conns.size)
        else // 有序: 固定连接
            i = (msg.subjectId % conns.size).toInt()

        return conns[i]
    }
}