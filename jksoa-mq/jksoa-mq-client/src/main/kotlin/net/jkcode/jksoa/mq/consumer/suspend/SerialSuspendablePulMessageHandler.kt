package net.jkcode.jksoa.mq.consumer.suspend

/**
 * 串行的可暂停的拉模式的消息处理器
 *    只用于拉模式的消费者上: MqPullConsumer
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-05 6:44 PM
 */
abstract class SerialSuspendablePullMessageHandler(
        public val exceptionSuspendSeconds: Int // 异常时暂停的秒数
): IMessageHandler(false) {

    init{
        if(exceptionSuspendSeconds <= 0)
            throw IllegalArgumentException("异常时暂停的秒数必须为正整数")
    }

    /**
     * 消费处理
     * @param msgs 消息
     */
    public override fun consumeMessages(msgs: Collection<Message>){
        try{
            doConsumeMessages(msgs)
        }catch(e: Exception){
            // 封装暂停的异常
            throw MqPullConsumeSuspendException(exceptionSuspendSeconds, e)
        }
    }

    /**
     * 真正的消费处理
     * @param msgs 消息
     */
    public abstract fun doConsumeMessages(msgs: Collection<Message>)
}