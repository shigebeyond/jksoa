package com.jksoa.mq.consumer.puller

import com.jksoa.common.CommonSecondTimer
import com.jksoa.common.CommonThreadPool
import com.jksoa.mq.consumer.subscriber.MqSubscriber
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit

/**
 * 消费拉取者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MessagePuller: IMessagePuller, MqSubscriber() {

    /**
     * 开始定时拉取消息
     */
    public override fun startPull(){
        // 一分钟拉取一次
        CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                for(topic in subscribedTopics)
                    pull(topic)
            }
        }, 60, TimeUnit.SECONDS)
    }

    /**
     * 拉取消息
     * @param topic
     */
    private fun pull(topic: String) {
        CommonThreadPool.execute() {
            // 拉取消息
            val msgs = broker.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!)
            // 处理消息
            for (msg in msgs)
                handleMessage(msg)
        }
    }
}