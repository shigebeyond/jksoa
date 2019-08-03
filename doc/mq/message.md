# 消息

主要属性

1. `topic` -- 消息主题
2. `data` -- 消息数据
3. `groupIds` -- 消费者分组id的比特集, 代表多个消费者分组id
4. `subjectId` -- 业务实体编号, 如订单号, 用于标识一系列的顺序消息
5. `id` -- 消息id, 但只在broker端保存时生成, 保证在同一个topic下有序

```
package net.jkcode.jksoa.mq.common

import java.io.Serializable
import java.util.*

/**
 * 消息
 *    id在broker端保存时生成, 保证在同一个topic下有序
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:54 PM
 */
data class Message(public val topic: String, // 主题
                   public val data: Any?, // 数据
                   public val groupIds: BitSet = BitSet(), // 分组id
                   public val subjectId: Long = 0 // 业务实体编号, 如订单号, 用于标识一系列的顺序消息
): Serializable {

    /**
     * 消息id, 但只在broker端保存时生成, 保证在同一个topic下有序
     *    对producer是无用的, 对broker+consumer有用
     */
    public var id: Long = 0
        protected set

}
```