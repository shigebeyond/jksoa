package net.jkcode.jksoa.rpc.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 缓存注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cache(
    public val keyPrefix: String = "", //  key的前缀
    public val keySeparator: String = "-", //  key的参数分隔符
    public val expires:Long, //  过期时间（秒）
    public val type: String = "jedis" // 缓存类型, 详见ICache接口与cache.yaml配置文件
)

/**
 * 获得缓存的注解
 */
public val Method.cache: Cache?
    get(){
        return getAnnotation(Cache::class.java)
    }