package net.jkcode.jksoa.sequence

/**
 * 序列号生成器
 * 　　为成员生成唯一的序列号
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-11 12:24 PM
 */
interface ISequence {

    /**
     * 模块
     */
    val module: String

    /**
     * 获得成员序号, 没有则创建
     * @param member
     * @return
     */
    fun getOrCreate(member: String): Int

    /**
     * 获得成员序号, 没有则抛异常
     * @param member
     * @return
     */
    fun get(member: String): Int
}