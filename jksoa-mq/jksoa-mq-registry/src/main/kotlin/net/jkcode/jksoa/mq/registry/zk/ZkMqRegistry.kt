package net.jkcode.jksoa.mq.registry.zk

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssigner
import net.jkcode.jksoa.mq.registry.toJson
import net.jkcode.jksoa.mq.registry.TopicRegex
import net.jkcode.jksoa.registry.IRegistry
import net.jkcode.jksoa.registry.zk.ZkRegistry

/**
 * 基于zookeeper的mq注册中心
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
object ZkMqRegistry: ZkMqDiscovery(), IMqRegistry {

    /**
     * rpc的注册中心
     */
    public val rpcRegistry: IRegistry = ZkRegistry

    /**
     * 注册topic = 给topic分配broker
     *
     * @param topic
     * @return false表示没有broker可分配
     */
    @Synchronized
    public override fun registerTopic(topic: String): Boolean {
        if(!TopicRegex.matches(topic))
            throw IllegalArgumentException("Invalid topic name: $topic")

        // 读topic分配
        val assignment = discover()

        // 读所有broker
        //val serviceId: String = IMqBroker::class.qualifiedName!! // 没依赖, 不能直接引用
        val serviceId: String = "net.jkcode.jksoa.mq.common.IMqBroker"
        val brokers = rpcRegistry.discover(serviceId)
        // 没有broker可分配
        if(brokers.isEmpty())
            return false

        // 分配者给topic分配broker
        val assigner = TopicAssigner(assignment, brokers)
        assigner.assignTopic(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
        return true
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    @Synchronized
    public override fun unregisterTopic(topic: String): Boolean {
        // 读topic分配
        val assignment = discover()

        // 已注销
        if(!assignment.containsKey(topic))
            return false

        // 删除topic
        assignment.remove(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
        return true
    }

    /**
     * 注销broker = 将该broker上的topic重新分配给其他broker
     *
     * @param removedBroker 被删除的broker
     * @param normalBrokers 正常的broker
     * @return false表示没有topic或broker可分配
     */
    @Synchronized
    public override fun unregisterBroker(removedBroker: String, normalBrokers: Collection<Url>): Boolean {
        if(normalBrokers.isEmpty())
            return false

        // 读topic分配
        val assignment = discover()
        if(assignment.isEmpty())
            return false

        // 分配者
        val assigner = TopicAssigner(assignment, normalBrokers)

        // 1 根据broker来删除topic
        val freeTopics = assigner.removeTopicsByBroker(removedBroker)

        // 2 给topic分配broker
        for(topic in freeTopics)
            assigner.assignTopic(topic)

        // 写topic分配
        zkClient.writeData(topic2brokerPath, assignment.toJson())
        return true
    }
}