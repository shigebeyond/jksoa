package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
interface IMqHandler {

    /**
     * 处理消息
     * @param msg 消息
     * @return 是否处理完成, 某些业务中不需或不能马上处理消息, 如短信平台处理繁忙不能马上处理
     */
    fun handleMessage(msg: Message): Boolean
}