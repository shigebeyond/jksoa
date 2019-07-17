package net.jkcode.jksoa.mq.broker

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkmvc.common.CommonMilliTimer
import net.jkcode.jksoa.mq.broker.pusher.MqPusher
import net.jkcode.jksoa.mq.broker.repository.lsm.LsmDelayMqRepository
import java.util.concurrent.TimeUnit

/**
 * 延迟消息发送的定时器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 5:17 PM
 */
object DelayMqDeliverTimer {

    /**
     * 启动定时发送
     */
    public fun start() {
        CommonMilliTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                // 获得延迟消息
                val msgs = LsmDelayMqRepository.pollExpiredDelayMessages()
                // 遍历消息来发送
                for(msg in msgs)
                    MqPusher.push(msg)

                start()
            }
        }, BrokerConfig.mqDelaySeconds, TimeUnit.SECONDS)
    }

}