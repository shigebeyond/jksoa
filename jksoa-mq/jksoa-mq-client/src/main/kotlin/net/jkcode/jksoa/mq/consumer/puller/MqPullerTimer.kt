package net.jkcode.jksoa.mq.consumer.puller

import net.jkcode.jkmvc.common.CommonSecondTimer
import net.jkcode.jkmvc.common.newPeriodic
import net.jkcode.jksoa.leader.ZkLeaderElection
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqLogger
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.subscriber.MqSubscriber
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

        if(started.compareAndSet(false, true)) {
            // 选举领导者: 一个组内只有一个拉取者
            val election = ZkLeaderElection("mqPuller/" + config["group"]!!)
            election.run() {
                start()
            }
        }
    }

    /**
     * 启动定时器
     */
    public override fun start(){
        // 一分钟拉取一次
        CommonSecondTimer.newPeriodic({
            for(topic in subscribedTopics)
                pull(topic)
        }, 600, TimeUnit.SECONDS)
    }

    /**
     * 拉取消息
     * @param topic
     */
    public override fun pull(topic: String) {
        commonPool.execute() {
            var msgs: List<Message>
            do {
                // 拉取消息
                msgs = broker.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!).get()
                // 处理消息 + 主动更新消息状态
                for (msg in msgs) {
                    // 异步处理消息
                    handleMessage(msg)
                }
            }while(msgs.isNotEmpty())
        }
    }
}