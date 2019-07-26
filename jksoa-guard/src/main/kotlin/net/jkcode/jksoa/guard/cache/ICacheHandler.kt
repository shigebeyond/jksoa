package net.jkcode.jksoa.guard.cache

import net.jkcode.jkmvc.cache.ICache
import net.jkcode.jksoa.client.combiner.annotation.Cache
import java.util.concurrent.CompletableFuture

/**
 * 缓存处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:39 PM
 */
abstract class ICacheHandler(public val annotation: Cache /* 该类不是通用工具类, 直接使用注解做属性 */) {

    /**
     * 尝试读缓存, 如果缓存不存在, 则回源并写缓存
     * @param args 方法参数, 用于组成缓存的key, 可以为空
     * @return
     */
    public fun cacheOrLoad(args: Array<Any?>): CompletableFuture<Any?> {
        val cache = ICache.instance(annotation.type) // cache
        val key = args.joinToString(annotation.keySeparator, annotation.keyPrefix) // key
        return cache.getOrPut(key, annotation.expires){ // data
            loadData(args) // 回源
        }
    }

    /**
     * 回源, 兼容返回值类型是CompletableFuture
     * @param args 方法参数, 用于组成缓存的key, 可以为空
     * @return
     */
    public abstract fun loadData(args: Array<Any?>):Any?


}