package com.jksoa.common.lock

import com.jkmvc.cache.JedisFactory
import redis.clients.jedis.Jedis

/**
 * 分布式锁接口
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class RedisLock(protected val configName: String = "default") : ILock {

    companion object {

        /**
         * 键的前缀
         */
        public val KeyPrefix: String = "lock/"

    }

    /**
     * redis连接
     */
    protected val jedis: Jedis
        get(){
            return JedisFactory.instance(configName)
        }


    /**
     * 加锁
     *
     * @param key 锁标识
     * @param expireTime 锁的过期时间, 单位秒
     * @return
     */
    public override fun lock(key: String, expireTime: Int): Boolean{
        val key = "$KeyPrefix$key"

        // 锁不住直接false
        if(jedis.setnx(key, "1") === 0L){
            // 处理设置过期时间失败的情况：直接删锁，下一个请求就正常了
            if(jedis.ttl(key) === -1L)
                jedis.del(key);

            return false;
        }

        // 锁n秒，注：此时可能进程中断，导致设置过期时间失败，则ttl = -1
        jedis.expire(key, expireTime)
        return true
    }

}