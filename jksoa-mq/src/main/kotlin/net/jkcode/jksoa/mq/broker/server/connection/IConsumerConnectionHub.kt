package net.jkcode.jksoa.mq.broker.server.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.mq.common.Message
import io.netty.channel.Channel
import net.jkcode.jksoa.common.IRpcRequest

interface IConsumerConnectionHub {
    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param channel
     */
    fun add(topic: String, group: String, channel: Channel)

    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param conn
     */
    fun add(topic: String, group: String, conn: NettyConnection)

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    fun select(req: IRpcRequest): IConnection?
}