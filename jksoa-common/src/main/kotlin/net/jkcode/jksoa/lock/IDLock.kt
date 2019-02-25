package net.jkcode.jksoa.lock

import net.jkcode.jkmvc.common.time

/**
 * 分布式锁接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
abstract class IDLock() {

    /**
     * 锁标识
     */
    public abstract val name: String

    /**
     * 数据
     */
    public abstract val data: String

    /**
     * 过期时间
     */
    protected var expireTime: Long? = null

    /**
     * 是否获得锁
     */
    public val locked: Boolean
        get() = expireTime != null && expireTime!! < time() // 未过期

    /**
     * 更新过期时间
     * @param expireSeconds 锁的过期时间, 单位秒
     */
    protected fun updateExpireTime(expireSeconds: Int) {
        expireTime = time() + expireSeconds * 1000
    }

    /**
     * 尝试加锁, 有过期时间
     *
     * @param expireSeconds 锁的过期时间, 单位秒
     * @return 是否加锁成功
     */
    public abstract fun attemptLock(expireSeconds: Int = 5): Boolean

    /**
     * 解锁
     */
    public abstract fun unlock()
}