package net.jkcode.jksoa.mq.connection

import io.netty.channel.Channel
import net.jkcode.jksoa.rpc.client.netty.NettyConnection
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.mq.common.GroupSequence
import java.net.InetSocketAddress

/**
 * 消费者连接使用的url
 *     主要是为了兼容 IConnectionHub 接口, 该接口都是使用 Url 来传参
 *     仅用在 ConsumerConnectionHub 中传递参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-19 6:53 PM
 */
data class ConsumerUrl(
        public val topic: String, // 主题
        public val groupId: Int, // 分组
        public val channel: Channel // 连接
): Url("netty", channel.remoteAddress() as InetSocketAddress) {

    // 构造函数
    public constructor(topic: String, group: String, channel: Channel): this(topic, GroupSequence.get(group), channel)

    /**
     * 连接
     */
    public val conn: NettyConnection = NettyConnection(channel, this)

}