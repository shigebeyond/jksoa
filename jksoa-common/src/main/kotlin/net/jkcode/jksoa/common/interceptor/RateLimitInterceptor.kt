package net.jkcode.jksoa.common.interceptor

import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jkmvc.ratelimit.IRateLimiter
import net.jkcode.jkmvc.ratelimit.LambdaRateLimiter
import net.jkcode.jkmvc.ratelimit.TokenBucketRateLimiter
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import java.util.concurrent.ConcurrentHashMap

/**
 * 限流的拦截器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-01 11:39 AM
 */
class RateLimitInterceptor(protected val serverSide: Boolean): Interceptor {

    /**
     * 限流器
     */
    protected val limiters: ConcurrentHashMap<String, IRateLimiter> = ConcurrentHashMap();

    /**
     * 前置处理请求
     * @param
     * @return
     */
    public override fun preHandleRequest(req: IRpcRequest): Boolean {
        // 获得限流器
        val limiter = limiters.getOrPutOnce(req.clazz + '.' + req.methodSignature){
            // 获得配置限流数
            val limit = if(serverSide) req.serverRateLimit else req.clientRateLimit
            if(limit == null) // 无配置, 直接通过
                LambdaRateLimiter{ true }
            else // 有配置, 则使用令牌桶限流器
                TokenBucketRateLimiter(limit)
        }
        return limiter.acquire()
    }
}