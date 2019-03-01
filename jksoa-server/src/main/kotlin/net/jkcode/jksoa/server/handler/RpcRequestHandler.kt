package net.jkcode.jksoa.server.handler

import net.jkcode.jkmvc.closing.ClosingOnRequestEnd
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.exception.RpcBusinessException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.provider.ProviderLoader
import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.interceptor.Interceptor

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object RpcRequestHandler : IRpcRequestHandler() {

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun doHandle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit {
        var res: RpcResponse? = null
        try{
            // 1 获得provider
            val provider = ProviderLoader.get(req.serviceId)
            if(provider == null)
                throw RpcServerException("服务[${req.serviceId}]没有提供者");

            // 2 获得方法
            val method = provider.getMethod(req.methodSignature)
            if(method == null)
                throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 3 调用方法, 构建响应对象
            try {
                val value = method.invoke(provider.service, *req.args)
                serverLogger.debug("Server处理请求：$req，结果: $value")
                res = RpcResponse(req.id, value)
            }catch (t: Throwable){
                throw RpcBusinessException(t) // 业务异常
            }
        }catch (t: Throwable){
            res = RpcResponse(req.id, t)
        }finally {
            // 4 返回响应
            ctx.writeAndFlush(res)

            // 请求处理后，关闭资源
            ClosingOnRequestEnd.triggerClosings()
        }
    }

}