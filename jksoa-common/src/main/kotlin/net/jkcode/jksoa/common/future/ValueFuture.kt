package net.jkcode.jksoa.common.future

import net.jkcode.jkmvc.future.ICallbackable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * 异步结果值
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ValueFuture(protected val resFuture: IRpcResponseFuture) : Future<Any?> by (resFuture as Future<Any?>), ICallbackable<Any?> by resFuture {

    /**
     * 同步获得结果，无超时
     * @return
     */
    public override fun get(): Any? {
        return resFuture.get().getOrThrow()
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        return resFuture.get(timeout, unit).getOrThrow()
    }

}
