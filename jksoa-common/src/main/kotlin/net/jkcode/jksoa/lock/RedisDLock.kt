package net.jkcode.jksoa.lock

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.redis.JedisFactory
import redis.clients.jedis.Jedis

/**
 * 分布式锁实现: redis锁
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class RedisDLock(public override val name: String/* 锁标识 */,
                 public override val data: String = Application.fullWorkerId /* 数据 */
) : IDLock() {

    companion object {

        /**
         * 键的前缀
         */
        public val KeyPrefix: String = "lock/"

        /**
         * 配置
         */
        public val config: IConfig = Config.instance("dlock", "yaml")

        /**
         * redis连接
         */
        protected val jedis: Jedis
            get(){
                return JedisFactory.instance(config["redisConfigName"]!!)
            }
    }

    /**
     * redis的key
     */
    protected val key = "$KeyPrefix$name"

    /**
     * 快速加锁, 锁不住不等待, 有过期时间
     *
     * @param expireSeconds 锁的过期时间, 单位秒
     * @return
     */
    public override fun quickLock(expireSeconds: Int): Boolean{
        if(locked) {
            // 更新过期时间
            jedis.expire(key, expireSeconds)
            return true
        }

        // 锁不住直接false
        if(jedis.setnx(key, data) === 0L){
            // 处理没有过期时间(即上一次设置过期时间失败)的情况：直接删锁，下一个请求就正常了
            if(jedis.ttl(key) === -1L)
                jedis.del(key);

            return false;
        }

        // 锁n秒，注：此时可能进程中断，导致设置过期时间失败，则ttl = -1
        jedis.expire(key, expireSeconds)

        // 更新过期时间
        updateExpireTime(expireSeconds)
        return true
    }

    /**
     * 解锁
     */
    public override fun unlock(){
        if(locked) // 未过期, 则删除key
            jedis.del(key)
        expireTime = null
    }

}