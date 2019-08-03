package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message

/**
 * 用lambda封装消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 7:56 PM
 */
class LambdaMessageHandler(
        concurrent: Boolean = true, // 是否线程池并发执行, 否则单线程串行执行
        protected val lambda: (Collection<Message>) -> Unit
) : IMessageHandler(concurrent) {

    /**
     * 处理消息
     * @param msgs 消息
     */
    public override fun consumeMessages(msgs: Collection<Message>) {
        lambda(msgs)
    }

}