package net.jkcode.jksoa.rpc.server

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkutil.ttl.SttlCurrentHolder
import net.jkcode.jkutil.ttl.RpcRequestScopedTransferableThreadLocal
import net.jkcode.jksoa.common.IRpcRequest

/**
 * rpc的server端上下文
 *   因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-4-17 5:52 PM
 */
data class RpcServerContext(public val req: IRpcRequest /* 请求 */,
                            public val ctx: ChannelHandlerContext /* netty channel上下文 */
){

    companion object: SttlCurrentHolder<RpcServerContext>(RpcRequestScopedTransferableThreadLocal()){ // rpc请求域的可传递的 ThreadLocal

        /**
         * 获得当前请求
         * @return
         */
        public fun currentRequest(): IRpcRequest {
            return current().req
        }

    }

    init {
        setCurrent(this)
    }

}