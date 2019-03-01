package net.jkcode.jksoa.common.interceptor

import net.jkcode.jkmvc.ratelimit.IRateLimiter
import net.jkcode.jkmvc.ratelimit.TokenBucketRateLimiter
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse

/**
 * 限流的拦截器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-01 11:39 AM
 */
class RateLimitInterceptor(limit: Int): Interceptor {

    /**
     * 限流器
     */
    protected val limiter: IRateLimiter = TokenBucketRateLimiter(limit)

    /**
     * 前置处理请求
     * @param
     * @return
     */
    public override fun preHandleRequest(req: IRpcRequest): Boolean {
        return limiter.acquire()
    }
}