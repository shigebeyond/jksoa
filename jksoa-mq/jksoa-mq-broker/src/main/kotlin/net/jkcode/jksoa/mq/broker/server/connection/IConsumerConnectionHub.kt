package net.jkcode.jksoa.mq.broker.server.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.common.IRpcRequest

/**
 * 消费者连接集中器
 *   消费者订阅主题+分组时, 收集该连接, 以便向其推送消息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:04 PM
 */
abstract class IConsumerConnectionHub: IConnectionHub() {

    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param conn
     */
    public abstract fun add(topic: String, group: String, conn: IConnection)

    /**
     * 删除连接
     *
     * @param topic
     * @param group
     * @param conn
     * @return
     */
    public abstract fun remove(topic: String, group: String, conn: IConnection): Boolean
}