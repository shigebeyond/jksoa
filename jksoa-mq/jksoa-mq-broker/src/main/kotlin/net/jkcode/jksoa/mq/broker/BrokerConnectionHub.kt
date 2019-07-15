package net.jkcode.jksoa.mq.broker

import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.loadbalance.ILoadBalancer
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.MqLoadBalancer
import net.jkcode.jksoa.mq.common.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 中转者连接集中器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-15 9:04 PM
 */
object BrokerConnectionHub : IConnectionHub {

}