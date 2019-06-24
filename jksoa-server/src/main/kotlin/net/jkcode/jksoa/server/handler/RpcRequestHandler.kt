package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkmvc.closing.ClosingOnRequestEnd
import net.jkcode.jkmvc.common.trySupplierFinally
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.exception.RpcBusinessException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.RpcContext
import net.jkcode.jksoa.server.provider.ProviderLoader

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
        trySupplierFinally({callProvider(req, ctx)} /* 调用provider方法 */){ r, e ->
            endResponse(req, r, e, ctx) // 返回响应
        }
    }

    /**
     * 调用provider方法
     * @param req
     * @param ctx
     * @return
     */
    private fun callProvider(req: IRpcRequest, ctx: ChannelHandlerContext): Any? {
        // 1 获得provider
        val provider = ProviderLoader.get(req.serviceId)
        if (provider == null)
            throw RpcServerException("服务[${req.serviceId}]没有提供者");

        // 2 获得方法
        val method = provider.getMethod(req.methodSignature)
        if (method == null)
            throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

        // 3 初始化rpc上下文: 因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
        RpcContext(req, ctx)

        // 4 调用方法
        return method.invoke(provider.service, *req.args)
    }

    /**
     * 返回响应, 在处理完请求后调用
     *
     * @param req
     * @param result 结果值
     * @param r 异常
     * @param ctx
     */
    private fun endResponse(req: IRpcRequest, result: Any?, r: Throwable?, ctx: ChannelHandlerContext) {
        var ex:Exception? = null
        if(r != null && r !is RpcServerException) // 封装业务异常
            ex = RpcBusinessException(r)

        serverLogger.debug("Server处理请求：{}，结果: {}, 异常: {}", req, result, r)
        // 返回响应
        var res: RpcResponse = RpcResponse(req.id, result, ex)
        ctx.writeAndFlush(res)

        // 请求处理后，关闭资源
        ClosingOnRequestEnd.triggerClosings()
    }
}