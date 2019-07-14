package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.Service
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@Service(onlyLeader = true)
interface IMqBroker : IService {

    /****************** 生产者调用 *****************/
    /**
     * 接收producer发过来的消息
     * @param msg 消息
     * @return
     */
    fun putMessage(msg: Message): CompletableFuture<Unit>

    /****************** 消费者调用 *****************/
    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    fun subscribeTopic(topic: String, group: String): CompletableFuture<Void>

    /**
     * 接受consumer的拉取消息
     * @param topic 主题
     * @param group 分组
     * @param limit 拉取记录数
     * @return
     */
    fun pullMessages(topic: String, group: String, limit: Int = 100): CompletableFuture<List<Message>>

    /**
     * 接受consumer的反馈消息消费结果
     * @param topic 主题
     * @param id 消息标识
     * @param e 消费异常
     * @return
     */
    fun feedbackMessage(topic: String, id: Long, e: Throwable?): CompletableFuture<Boolean>

}