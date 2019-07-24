package net.jkcode.jksoa.mq.registry

import net.jkcode.jksoa.common.Url
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Mq服务发现的监听器的容器：监听topic分配变化
 *    维护与代理多个 IMqDiscoveryListener
 *    自身缓存了topic分配信息, 通过做本地topic分配信息对比, 从而辨别出真正的topic分配信息变化, 才会触发 IMqDiscoveryListener, 从而减少重复触发
 *    如在 ZkMqDataListener 的实现中通过zk数据变化, 从而触发 handleTopic2BrokerChange(新topic分配信息), 进而触发 IMqDiscoveryListener
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
open class MqDiscoveryListenerContainer(
        protected val list: MutableList<IMqDiscoveryListener> = LinkedList() // 代理list
): IMqDiscoveryListener, MutableList<IMqDiscoveryListener> by list {

    /**
     * 缓存topic分配信息
     */
    protected var assignment: TopicAssignment = EmptyTopicAssignment

    /**
     * 处理topic分配变化
     *
     * @param assignment
     */
    public override fun handleTopic2BrokerChange(assignment: TopicAssignment) {
        // 对比本地缓存, 变化了才触发 IMqDiscoveryListener
        if(this.assignment.equals(assignment))
            return

        // 触发变化
        this.assignment = assignment
        for(l in list)
            l.handleTopic2BrokerChange(assignment)
    }

    /**
     * 添加子监听器, 要自动通知topic分配信息
     */
    public override fun add(listener: IMqDiscoveryListener): Boolean{
        if(assignment != EmptyTopicAssignment)
            listener.handleTopic2BrokerChange(assignment)

        return list.add(listener)
    }

    /**
     * 添加子监听器, 要自动通知topic分配信息
     */
    public override fun addAll(listeners: Collection<IMqDiscoveryListener>): Boolean{
        if(assignment != EmptyTopicAssignment)
            for(l in listeners)
                l.handleTopic2BrokerChange(assignment)

        return list.addAll(listeners)
    }

    /**
     * 添加子监听器, 要自动通知topic分配信息
     */
    public override fun addAll(index: Int, listeners: Collection<IMqDiscoveryListener>): Boolean{
        if(assignment != EmptyTopicAssignment)
            for(l in listeners)
                l.handleTopic2BrokerChange(assignment)

        return list.addAll(index, listeners)
    }
    

}