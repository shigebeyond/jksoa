package com.jksoa.common.lock

/**
 * 分布式锁接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
interface ILock {

    /**
     * 加锁
     *
     * @param key 锁标识
     * @param expireTime 锁的过期时间, 单位秒
     * @return
     */
    fun lock(key: String, expireTime: Int = 3): Boolean

}