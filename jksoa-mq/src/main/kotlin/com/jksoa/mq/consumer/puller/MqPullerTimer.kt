package com.jksoa.mq.consumer.puller

import com.jkmvc.common.getStackTrace
import com.jkmvc.common.stringifyStackTrace
import com.jksoa.common.CommonSecondTimer
import com.jksoa.common.CommonThreadPool
import com.jksoa.mq.common.MessageStatus
import com.jksoa.mq.consumer.IMqHandler
import com.jksoa.mq.consumer.subscriber.MqSubscriber
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 消费拉取者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MqPullerTimer: IMqPullerTimer, MqSubscriber() {

    /**
     * 是否已启动
     */
    private val started: AtomicBoolean = AtomicBoolean(false)

    /**
     * 订阅主题 -- 启动定时器
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        super.subscribeTopic(topic, handler)

        if(started.compareAndSet(false, true))
            start()
    }

    /**
     * 启动定时器
     */
    public override fun start(){
        // 一分钟拉取一次
        CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                for(topic in subscribedTopics)
                    pull(topic)

                start()
            }
        }, 60, TimeUnit.SECONDS)
    }

    /**
     * 拉取消息
     * @param topic
     */
    public override fun pull(topic: String) {
        CommonThreadPool.execute() {
            // 拉取消息
            val msgs = broker.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!)
            // 处理消息 + 主动更新消息状态
            for (msg in msgs) {
                try {
                    // true表示处理完成, false表示未处理
                    if(handleMessage(msg))
                        broker.updateMessage(msg.id, MessageStatus.DONE)
                }catch (e: Exception){
                    // Exception对象表示处理异常
                    broker.updateMessage(msg.id, MessageStatus.FAIL, "Exception: " + e.stringifyStackTrace())
                }
            }
        }
    }
}