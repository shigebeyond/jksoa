package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.exception.RpcBusinessException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.server.RpcContext
import net.jkcode.jksoa.server.provider.ProviderLoader
import java.util.concurrent.CompletableFuture

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
        var value:Any? = null
        var ex: Exception? = null
        try{
            // 1 获得provider
            val provider = ProviderLoader.get(req.serviceId)
            if(provider == null)
                throw RpcServerException("服务[${req.serviceId}]没有提供者");

            // 2 获得方法
            val method = provider.getMethod(req.methodSignature)
            if(method == null)
                throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 3 初始化rpc上下文: 因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
            RpcContext(req, ctx)

            // 4 调用方法, 构建响应对象
            try {
                value = method.invoke(provider.service, *req.args)
                // 4.1 异步结果: 处理 CompletableFuture 类型的返回值形式
                if(value is CompletableFuture<*>)
                    value.whenComplete { value, t ->
                        val ex: Exception? = if(t == null) null else RpcBusinessException(t)
                        endResponse(req, value, ex, ctx)
                    }
            }catch (t: Throwable){
                throw RpcBusinessException(t) // 业务异常
            }
        }catch (e: Exception){
            ex = e
        }finally {
            // 4.2 同步结果
            if(value !is CompletableFuture<*>)
                endResponse(req, value, ex, ctx)
        }
    }

}