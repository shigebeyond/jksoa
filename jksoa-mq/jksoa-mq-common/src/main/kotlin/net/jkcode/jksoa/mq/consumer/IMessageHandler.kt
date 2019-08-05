package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
abstract class IMessageHandler(public val concurrent: Boolean = true /* 是否线程池并发执行, 否则单线程串行执行 */) {

    /**
     * 消费处理
     * @param msgs 消息
     */
    public abstract fun consumeMessages(msgs: Collection<Message>)
}