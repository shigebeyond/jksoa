package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.consumer.service.IMqConsumerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.subscriber.IMqSubscriber
import net.jkcode.jksoa.mq.consumer.subscriber.MqSubscriber
import net.jkcode.jksoa.server.provider.ProviderLoader

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
object MqConsumer(public override val isPuller: Boolean = false /* 是否拉模式 */ ) : IMqConsumer, IMqSubscriber by MqSubscriber {

    /**
     * 消息中转者
     */
    protected val broker = Referer.getRefer<IMqBrokerService>()

    /**
     * 启动者
     */
    private val starter = AtomicStarter()

    init {
        // 提供消费者服务, 但不用注册到注册中心
        ProviderLoader.addClass(MqConsumer::class.java, false)
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        // 推模式: 向中转者订阅主题, 然后中转者就会向你推消息, 推送处理见 MqConsumerService
        if(isPush){
            // 调用代理的实现
            MqSubscriber.subscribeTopic(topic, handler)

            // 向中转者订阅主题
            broker.subscribeTopic(topic, config["group"]!!)
            return
        }

        // 拉模式: 选举领导者, 一个组内只有一个拉取者
        val election = ZkLeaderElection("mqPuller/" + config["group"]!!)
        election.run() {
            // 调用代理的实现
            MqSubscriber.subscribeTopic(topic, handler)

            // 启动拉取定时器
            starter.startOnce{
                startPullTimer()
            }
        }
    }

    /******************** 拉模式实现 *******************/
    /**
     * 启动拉取定时器
     */
    private fun startPullTimer(){
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