package net.jkcode.jksoa.mq.consumer.client.connection

import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.loadbalance.ILoadBalancer
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.MqLoadBalancer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 中转者连接集中器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-15 9:04 PM
 */
class BrokerConnectionHub : ConnectionHub() {

    /**
     * 处理服务地址删除
     * @param url
     */
    protected override fun handleServiceUrlRemove(serverName: String) {
        super.handleServiceUrlRemove(serverName)
        ZkMqRegistry.unregisterBroker(serverName, connections.values)
    }

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {

    }

    /**
     * 选择全部连接
     *
     * @return
     */
    public override fun selectAll(): Collection<IConnection> {

    }

}