package net.jkcode.jksoa.mq.registry

import net.jkcode.jksoa.common.Url
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * topic分配者
 * 
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
class TopicAssigner(public val assignment: TopicAssignment, brokers: Collection<Url>){

    companion object{
        /**
         * broker负载比较器
         */
        public val brokerLoadComparator = object: Comparator<Pair<*, AtomicInteger>> {

            public override fun compare(o1: Pair<*, AtomicInteger>, o2: Pair<*, AtomicInteger>): Int {
                return o1.second.get() - o2.second.get()
            }
        }
    }

    /**
     * 构建broker负载
     * @param assignment
     * @param brokers
     * @return
     */
    protected val brokerLoads: List<Pair<String, AtomicInteger>> by lazy{
        // 统计broker负载
        val brokerLoads: List<Pair<String, AtomicInteger>> = brokers.map { url ->
            url.serverName to AtomicInteger()
        }
        for (url in assignment.values) {
            val counter = brokerLoads.first {
                it.first == url
            }
            counter.second.incrementAndGet() // 计数+1
        }
        brokerLoads
    }

    /**
     * 给topic分配broker
     * @param topic
     * @return
     */
    public fun assignTopic(topic: String): TopicAssignment {
        // 已分配过
        if(assignment.containsKey(topic))
            return assignment

        // 选出最小负载的broker
        val minBroker = pickMinBroker()

        // 将broker分配给topic
        assignment[topic] = minBroker
        return assignment
    }

    /**
     * 选出最小负载的broker
     *    有副作用:计数+1
     * @return
     */
    protected fun pickMinBroker(): String {
        val brokerLoad = Collections.min(brokerLoads, brokerLoadComparator)
        brokerLoad.second.incrementAndGet() // 计数+1
        return brokerLoad.first
    }

    /**
     * 根据broker来删除topic
     * @param broker
     * @return
     */
    public fun removeTopicsByBroker(broker: String): LinkedList<String> {
        val freeTopics = LinkedList<String>()
        for((topic, oldBroker) in assignment)
            if(oldBroker == broker) {
                assignment.remove(topic)
                freeTopics.add(topic)
            }

        return freeTopics
    }
}