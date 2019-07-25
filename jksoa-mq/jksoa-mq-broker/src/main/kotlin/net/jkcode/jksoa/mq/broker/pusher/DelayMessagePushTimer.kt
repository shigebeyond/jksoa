package net.jkcode.jksoa.mq.broker.pusher

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkmvc.common.CommonMilliTimer
import net.jkcode.jksoa.mq.broker.BrokerConfig
import net.jkcode.jksoa.mq.broker.repository.lsm.LsmDelayMessageRepository
import java.util.concurrent.TimeUnit

/**
 * 延迟消息发送的定时器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 5:17 PM
 */
object DelayMessagePushTimer {

    /**
     * 启动定时发送
     */
    public fun start() {
        CommonMilliTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                // 获得延迟消息
                val msgs = LsmDelayMessageRepository.pollExpiredDelayMessages(){ msgs ->
                    // 遍历消息来发送
                    for(msg in msgs)
                        MqPusher.pushMessage(msg)
                }

                start()
            }
        }, BrokerConfig.mqDelaySeconds, TimeUnit.SECONDS)
    }

}