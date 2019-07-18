package net.jkcode.jksoa.sequence

/**
 * 序列号生成器: 基于zk的持久顺序节点来实现
 * 　　为成员生成唯一的序列号
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-11 12:24 PM
 */
interface ISequenceIdGenerator {

    /**
     * 模块
     */
    val module: String

    /**
     * 获得成员序号
     */
    fun getSequenceId(member: String): Int
}