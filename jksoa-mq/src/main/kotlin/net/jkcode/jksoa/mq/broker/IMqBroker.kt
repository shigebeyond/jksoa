package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.common.annotation.ServiceMethodMeta
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MessageStatus
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
    fun postMessage(msg: Message): CompletableFuture<Void>

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
     * @param pageSize 每页记录数
     * @return
     */
    fun pullMessages(topic: String, group: String, pageSize: Int): CompletableFuture<List<Message>>

    /**
     * 接受consumer的更新消息
     * @param id 消息标识
     * @param status 状态: 0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)
     * @param remark 备注
     * @return
     */
    fun updateMessage(id: Long, status: MessageStatus, remark: String? = null): CompletableFuture<Boolean>

}