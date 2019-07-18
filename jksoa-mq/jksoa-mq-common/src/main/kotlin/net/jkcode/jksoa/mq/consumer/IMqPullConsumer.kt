package net.jkcode.jksoa.mq.consumer

/**
 * 拉模式的消息消费者
 *    有拉取的定时器
 *    一个消费组内只有一个拉取者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IMqPullConsumer : IMqSubscriber {

    /**
     * 启动拉取定时器
     */
    fun start()

}