package net.jkcode.jksoa.mq.consumer

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.leader.ZkLeaderElection
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.TimeUnit

/**
 * 拉模式的消息消费者
 *    有拉取的定时器
 *    一个消费组内只有一个拉取者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
object MqPullConsumer : IMqPullConsumer, IMqSubscriber by MqSubscriber {

    /**
     * consumer配置
     */
    public val config = Config.instance("consumer", "yaml")

    /**
     * 消息中转者
     */
    private val brokerService = Referer.getRefer<IMqBrokerService>()

    /**
     * 启动者
     */
    private val starter = AtomicStarter()

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        // 拉模式: 选举领导者, 一个组内只有一个拉取者
        val election = ZkLeaderElection("mqPuller/" + config["group"]!!)
        election.run() {
            // 调用代理的实现
            MqSubscriber.subscribeTopic(topic, handler)

            // 启动拉取定时器
            starter.startOnce{
                start()
            }
        }
    }

    /**
     * 启动拉取定时器
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
    private fun pull(topic: String) {
        CommonThreadPool.execute() {
            var msgs: List<Message>
            do {
                // 拉取消息
                msgs = brokerService.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!).get()
                // 处理消息 + 主动更新消息状态
                for (msg in msgs) {
                    // 异步处理消息
                    handleMessage(msg)
                }
            }while(msgs.isNotEmpty())
        }
    }
}