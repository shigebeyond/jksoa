package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.sequence.ISequence
import net.jkcode.jksoa.sequence.ZkSequence

/**
 * 主题id生成器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-19 7:37 PM
 */
object TopicSequence : ISequence by ZkSequence.instance("mqTopic"){

}