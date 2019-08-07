package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.annotation.RemoteMethod
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.connection.BrokerConnectionHub
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者服务
 *    由于broker server端做了请求(消息)的定时定量处理, 因此请求超时需增大, 详见注解 @RemoteMethod.requestTimeoutMillis
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@RemoteService(connectionHubClass = BrokerConnectionHub::class)
interface IMqBrokerService {

    /****************** 生产者调用 *****************/
    /**
     * 接收producer发过来的单个消息
     * @param msg 消息
     * @return 消息id
     */
    @RemoteMethod(1000)
    fun putMessage(msg: Message): CompletableFuture<Long>

    /**
     * 批量接收producer发过来的多个消息
     *    优化: client在调用前先校验消息是否是同一个主题, 实现见 BrokerConnectionHub.checkBeforePutMessages()
     *
     * @param topic 主题
     * @param msgs 同一个主题的多个消息
     * @return 消息id
     */
    @RemoteMethod(1000)
    fun putMessages(topic: String, msgs: List<Message>): CompletableFuture<Array<Long>>

    /****************** 消费者调用 *****************/
    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    fun subscribeTopic(topic: String, group: String): CompletableFuture<Unit>

    /**
     * 接受consumer的按分组来拉取消息
     *    无关读进度
     *
     * @param topic 主题
     * @param group 分组
     * @param startId 开始的消息id
     * @param limit 拉取记录数
     * @return
     */
    fun pullMessagesByGroup(topic: String, group: String, startId: Long, limit: Int = 100): CompletableFuture<List<Message>>

    /**
     * 接受consumer的按分组读进度来拉取消息
     *    按上一次的读进度来开始读下一页
     *    保存当前读进度
     *
     * @param topic 主题
     * @param group 分组
     * @param limit 拉取记录数
     * @return
     */
    fun pullMessagesByGroupProgress(topic: String, group: String, limit: Int = 100): CompletableFuture<List<Message>>

    /**
     * 接受consumer的反馈消息消费结果
     * @param topic 主题
     * @param group 分组
     * @param ids 消息标识
     * @param e 消费异常
     * @return
     */
    @RemoteMethod(1000)
    fun feedbackMessages(topic: String, group: String, id: List<Long>, e: Throwable? = null): CompletableFuture<Unit>

}