package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.annotation.RemoteService

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@RemoteService(onlyLeader = true)
interface IMqBrokerLeaderService {

    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    fun registerTopic(topic: String): Boolean

    /**
     * 注销主题
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    fun unregisterTopic(topic: String): Boolean

}