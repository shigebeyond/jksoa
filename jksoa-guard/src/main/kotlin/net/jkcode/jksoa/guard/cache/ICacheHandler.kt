package net.jkcode.jksoa.guard.cache

import net.jkcode.jkmvc.cache.ICache
import net.jkcode.jksoa.client.combiner.annotation.Cache

/**
 * 缓存处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:39 PM
 */
abstract class ICacheHandler(public val annotation: Cache) {

    /**
     * 尝试读缓存, 如果缓存不存在, 则回源并写缓存
     * @param args
     * @return
     */
    public fun cacheOrLoad(args: Array<Any?>): Any? {
        val cache = ICache.instance(annotation.type)
        val key = args.joinToString(annotation.keySeparator, annotation.keyPrefix)
        return cache.getOrPut(key, annotation.expires){
            loadData(args) ?: Unit // 回源 or 空对象
        }
    }

    /**
     * 回源
     * @param args
     * @return
     */
    public abstract fun loadData(args: Array<Any?>):Any?


}