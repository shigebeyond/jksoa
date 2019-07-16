package net.jkcode.jksoa.mq.registry

/**
 * 处理topic分配变化
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
interface IMqDiscoveryListener {

    /**
     * 处理topic分配变化
     *
     * @param assign
     */
    fun handleTopic2BrokerChange(assign: TopicAssignment)

}