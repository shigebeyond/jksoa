package com.jksoa.mq.broker.server.connection

import com.jksoa.client.IConnection
import com.jksoa.client.protocol.netty.NettyConnection
import com.jksoa.mq.common.Message
import io.netty.channel.Channel

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
     * @param msg
     * @return
     */
    fun select(msg: Message): IConnection?{
        return select(msg.topic, msg.group)
    }

    /**
     * 选择一个连接
     *
     * @param topic
     * @param group
     * @return
     */
    fun select(topic: String, group: String): IConnection?
}