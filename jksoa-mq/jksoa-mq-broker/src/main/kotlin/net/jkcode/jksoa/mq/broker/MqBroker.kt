package net.jkcode.jksoa.mq.broker

import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.broker.repository.DbMessageRepository
import net.jkcode.jksoa.mq.broker.repository.IMessageRepository
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.common.IMqBroker
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.server.RpcContext
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {

    companion object{
        /**
         * 中转者者配置
         */
        public val config = Config.instance("broker", "yaml")

        /**
         * 主题对分组的映射
         */
        public val topic2group: Map<String, List<String>> = config["topic2group"]!!

        /**
         * 消息仓库
         */
        public val repository: IMessageRepository = DbMessageRepository()
    }

    /****************** 生产者调用 *****************/
    /**
     * 消息合并器
     *    消息入队, 合并存储
     */
    protected val msgCombiner = GroupRunCombiner(100, 100, this::saveMessages)

    /**
     * 接收producer发过来的消息
     * @param msg 消息
     * @return
     */
    public override fun putMessage(msg: Message): CompletableFuture<Unit> {
        return msgCombiner.add(msg)
    }

    /**
     * 批量保存消息
     * @param msgs
     */
    public fun saveMessages(msgs: List<Message>){
        repository.saveMessages(msgs)
    }

    /****************** 消费者调用 *****************/
    /**
     * 消费者连接集中器
     */
    public val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    public override fun subscribeTopic(topic: String, group: String): CompletableFuture<Void> {
        // 记录连接
        val ctx = RpcContext.current().ctx
        val channel = ctx.channel()
        val conn = NettyConnection(channel)
        connHub.add(topic, group, conn)

        // channel关闭时删除连接
        channel.closeFuture().addListener {
            connHub.remove(topic, group, conn)
        }

        return CompletableFuture.completedFuture(null)
    }

    /**
     * 接受consumer的拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    public override fun pullMessages(topic: String, group: String, pageSize: Int): CompletableFuture<List<Message>> {
        val msgs = repository.getMessagesByTopicAndGroup(topic, group, pageSize)

        return CompletableFuture.completedFuture(msgs)
    }


    /**
     * 接受consumer的反馈消息消费结果
     * @param id 消息标识
     * @param e 消费异常
     * @return
     */
    public override fun feedbackMessage(id: Long, e: Throwable?): CompletableFuture<Boolean> {
        val r = repository.deleteMsg(id)
        return CompletableFuture.completedFuture(r)
    }

}