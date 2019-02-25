package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.mq.common.Message

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IMqConsumer : IService  {

    /**
     * 收到推送的消息
     * @param msg 消息
     * @return
     */
    fun pushMessage(msg: Message): Boolean

}