package net.jkcode.jksoa.mq.registry

/**
 * topic分配变化的监听器
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
interface IMqDiscoveryListener {

    /**
     * 处理topic分配变化
     *
     * @param assignment
     */
    fun handleTopic2BrokerChange(assignment: TopicAssignment)

}