package com.jksoa.mq.common

import com.jkmvc.common.generateId

/**
 * 消息
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:54 PM
 */
data class Message(public val topic: String /* 主题 */,
                   public val data: Any? /* 数据 */,
                   public val group: String? = null /* 分组 */,
                   val id: Long = generateId() /* 消息标识，全局唯一 */
) {
}