package com.jksoa.lock

/**
 * 分布式锁接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
abstract class IDLock {

    /**
     * 锁标识
     */
    public abstract val name: String

    /**
     * 过期时间
     */
    protected var expireTime: Long? = null

    /**
     * 是否获得锁
     */
    public val locked: Boolean
        get() = expireTime != null && expireTime!! < System.currentTimeMillis() // 未过期

    /**
     * 尝试加锁
     *
     * @param expireSeconds 锁的过期时间, 单位秒
     * @return
     */
    public abstract fun attemptLock(expireSeconds: Int = 5): Boolean

    /**
     * 解锁
     */
    public abstract fun unlock()
}