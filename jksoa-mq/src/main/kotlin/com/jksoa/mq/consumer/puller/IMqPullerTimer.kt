package com.jksoa.mq.consumer.puller

import com.jksoa.mq.common.Message

/**
 * 消费拉取者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
interface IMqPullerTimer {

    /**
     * 启动定时器
     */
    fun start()

    /**
     * 拉取消息
     * @param topic
     */
    fun pull(topic: String)
}