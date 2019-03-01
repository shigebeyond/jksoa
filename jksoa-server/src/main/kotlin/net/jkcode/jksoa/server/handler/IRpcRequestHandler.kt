package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.interceptor.Interceptor
import net.jkcode.jksoa.common.interceptor.RateLimitInterceptor
import java.util.*

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
abstract class IRpcRequestHandler {

    companion object {

        /**
         * 服务端配置
         */
        public val config = Config.instance("server", "yaml")
    }

    /**
     * 拦截器
     */
    protected val interceptors: List<Interceptor> by lazy{
        val rateLimit:Int? = config["rateLimit"]
        if(rateLimit == null)
            emptyList<Interceptor>()
        else
            listOf(RateLimitInterceptor(rateLimit))
    }

    /**
     * 处理请求
     *
     * @param req
     */
    public fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit{
        // 调用拦截器
        for(i in interceptors)
            if(!i.preHandleRequest(req))
                throw RpcServerException("Interceptor [${i.javaClass.name}] handle request fail");

        // 内部处理
        doHandle(req, ctx)
    }


    /**
     * 处理请求
     *
     * @param req
     */
    public abstract fun doHandle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit
}