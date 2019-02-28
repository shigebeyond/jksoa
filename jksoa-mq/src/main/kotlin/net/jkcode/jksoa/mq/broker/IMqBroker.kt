package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.ServiceMeta
import net.jkcode.jksoa.common.annotation.ServiceMethodMeta
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MessageStatus

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@ServiceMeta(onlyLeader = true)
interface IMqBroker : IService {

    /****************** 生产者调用 *****************/
    /**
     * 发送消息
     * @param msg 消息
     */
    @ServiceMethodMeta(requestTimeoutMillis = 300)
    fun postMessage(msg: Message)

    /****************** 消费者调用 *****************/
    /**
     * 订阅主题
     * @param topic 主题
     * @param group 分组
     */
    fun subscribeTopic(topic: String, group: String)

    /**
     * 拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    fun pullMessages(topic: String, group: String, pageSize: Int): List<Message>

    /**
     * 更新消息
     * @param id 消息标识
     * @param status 状态: 0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)
     * @param remark 备注
     * @return
     */
    fun updateMessage(id: Long, status: MessageStatus, remark: String? = null): Boolean

}