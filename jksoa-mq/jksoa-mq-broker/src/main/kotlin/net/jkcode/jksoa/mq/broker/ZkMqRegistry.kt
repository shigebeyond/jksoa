package net.jkcode.jksoa.mq.broker

import net.jkcode.jkmvc.orm.toJson
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.mq.common.IMqBroker
import net.jkcode.jksoa.registry.IRegistry
import net.jkcode.jksoa.registry.zk.ZkRegistry

/**
 * 基于zookeeper的mq注册中心
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
object ZkMqRegistry: ZkMqDiscovery() {

    /**
     * rpc的注册中心
     */
    public val rpcRegistry: IRegistry = ZkRegistry

    /**
     * 注册topic = 给topic分配broker
     *
     * @param topic
     * @return
     */
    public fun registerTopic(topic: String) {
        // 读topic分配
        val assignment = discover()

        // 分配者给topic分配broker
        val assigner = TopicAssigner(assignment, getAllBrokers())
        assigner.assignTopic(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
    }

    /**
     * 获得所有的broker
     * @return
     */
    private fun getAllBrokers(): List<Url> {
        return rpcRegistry.discover(IMqBroker::class.qualifiedName!!)
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return
     */
    public fun unregisterTopic(topic: String) {
        // 读topic分配
        val assignment = discover()

        // 已注销
        if(!assignment.containsKey(topic))
            return

        // 删除topic
        assignment.remove(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
    }

    /**
     * 注销broker = 将removedBroker上的topic重新分配给normalBrokers
     *
     * @param removedBroker 被删除的broker
     * @param normalBrokers 正常的broker
     * @return
     */
    public fun unregisterBroker(removedBroker: Url, normalBrokers: List<Url>) {
        // 读topic分配
        val assignment = discover()

        // 分配者
        val assigner = TopicAssigner(assignment, normalBrokers)

        // 1 根据broker来删除topic
        val freeTopics = assigner.removeTopicsByBroker(removedBroker.serverName)

        // 2 给topic分配broker
        for(topic in freeTopics)
            assigner.assignTopic(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
    }
}