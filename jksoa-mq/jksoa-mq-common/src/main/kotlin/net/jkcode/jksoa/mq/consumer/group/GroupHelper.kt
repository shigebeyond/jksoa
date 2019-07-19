package net.jkcode.jksoa.mq.consumer.group

import net.jkcode.jksoa.sequence.ZkSequenceIdGenerator

/**
 * 消费者分组
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-19 7:37 PM
 */
object GroupHelper {

    /**
     * 分组id生成器
     */
    public val groupIdGenerator = ZkSequenceIdGenerator.instance("mqGroup")

    /**
     * 获得分组id
     */
    public fun getGroupId(group: String): Int {
        return groupIdGenerator.getSequenceId(group)
    }
}