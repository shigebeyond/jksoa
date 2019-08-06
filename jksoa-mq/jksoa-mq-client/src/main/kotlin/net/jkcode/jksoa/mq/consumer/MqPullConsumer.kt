package net.jkcode.jksoa.mq.consumer

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.leader.ZkLeaderElection
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqClientLogger
import net.jkcode.jksoa.mq.consumer.suspend.MqPullConsumeSuspendException
import net.jkcode.jksoa.mq.consumer.suspend.PullConsumeSuspendProgress
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
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
     * 分组
     */
    private val group: String = config["group"]!!

    /**
     * 每次拉取记录数
     */
    private val limit: Int = config.getInt("pullPageSize", 100)!!

    /**
     * 消息中转者
     */
    private val brokerService = Referer.getRefer<IMqBrokerService>()

    /**
     * 启动者
     */
    private val starter = AtomicStarter()

    /**
     * 主题的暂停进度
     */
    private val topicSuspendProgresses: ConcurrentHashMap<String, PullConsumeSuspendProgress> = ConcurrentHashMap();

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler 消息处理器
     */
    public override fun subscribeTopic(topic: String, handler: IMessageHandler){
        // 拉模式: 选举领导者, 一个组内只有一个拉取者
        val election = ZkLeaderElection("mqPuller/" + config["group"]!! + "-" + topic)
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
        }, config["pullTimerSeconds"]!!, TimeUnit.SECONDS)
    }

    /**
     * 拉取消息
     * @param topic
     */
    private fun pull(topic: String) {
        // 在暂停时期内, 不拉取
        val suspendProgress = topicSuspendProgresses[topic]
        if(suspendProgress != null && currMillis() < suspendProgress.endTime)
            return

        CommonThreadPool.execute() {
            // 拉取消息
            val msgsFuture: CompletableFuture<List<Message>>
            if(suspendProgress == null) // 按上一次的读进度来拉取
                msgsFuture = brokerService.pullMessagesByGroupProgress(topic, group, limit)
            else{ // 按暂停进度来拉取
                msgsFuture = brokerService.pullMessagesByGroup(topic, group, suspendProgress.startId, limit)
                topicSuspendProgresses.remove(topic)
            }
            val msgs = msgsFuture.get()

            if(msgs.isNotEmpty()) {
                // 异步消费消息, 消费完给broker反馈消费结果
                val future = consumeMessages(topic, msgs)
                future.whenComplete { r, ex ->
                    if(ex is MqPullConsumeSuspendException){ // 有消费暂停的异常 => 暂停
                        mqClientLogger.debug("消费主题[{}]消息出错, 暂停拉取定时器{}s", topic, ex.suspendSeconds)
                        topicSuspendProgresses[topic] = PullConsumeSuspendProgress(currMillis() + ex.suspendSeconds * 1000, msgs.first().id)
                    }else{ // 继续: 递归调用
                        pull(topic)
                    }
                }
            }
        }
    }
}