package net.jkcode.jksoa.mq.common

import net.jkcode.jkmvc.common.generateId
import java.util.*

/**
 * 消息
 *    id在broker端保存时生成, 保证在同一个topic下有序
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:54 PM
 */
data class Message(public val topic: String /* 主题 */,
                   public val data: Any? /* 数据 */,
                   public val group: String = "*" /* 分组, 如果是*, 则标识广播所有分组 */,
                   public val subjectId: Long = 0 /* 业务实体编号, 如订单号, 用于标识一系列的顺序消息 */
) {

    /**
     * 消息id, 但只在broker端保存时生成, 保证在同一个topic下有序
     *    对producer是无用的, 对broker+consumer有用
     */
    public var id: Long = 0
        protected set

    public constructor(topic: String , data: Any? , subjectId: Long): this(topic, data, "*", subjectId)
}