package net.jkcode.jksoa.mq.common

import net.jkcode.jkutil.sequence.ISequence
import net.jkcode.jkutil.sequence.ZkSequence

/**
 * 消费者分组id生成器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-19 7:37 PM
 */
object GroupSequence : ISequence by ZkSequence.instance("mqGroup"){

}