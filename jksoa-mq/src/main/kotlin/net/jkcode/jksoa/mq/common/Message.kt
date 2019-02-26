package net.jkcode.jksoa.mq.common

import net.jkcode.jkmvc.common.generateId
import java.util.*

/**
 * 消息
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:54 PM
 */
data class Message(public val topic: String /* 主题 */,
                   public val data: Any? /* 数据 */,
                   public val group: String = "*" /* 分组, 如果是*, 则标识广播所有分组 */,
                   public val orderId: Long = 0, /* 顺序id */
                   public val id: Long = generateId("mq") /* 消息标识，全局唯一 */
) {
}