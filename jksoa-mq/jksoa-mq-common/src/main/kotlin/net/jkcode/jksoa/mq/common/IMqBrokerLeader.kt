package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.common.annotation.RemoteService

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@RemoteService(onlyLeader = true)
interface IMqBrokerLeader {

    /**
     * 创建主题
     * @param topic 主题
     * @return
     */
    fun createTopic(topic: String)

    /**
     * 创建分组
     * @param group 分组
     * @return
     */
    fun createGroup(group: String)

}