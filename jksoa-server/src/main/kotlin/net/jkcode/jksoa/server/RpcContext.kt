package net.jkcode.jksoa.server

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.IRpcRequest

/**
 * rpc上下文
 *   因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-4-17 5:52 PM
 */
data class RpcContext(public val req: IRpcRequest /* 请求 */,
                      public val ctx: ChannelHandlerContext /* netty channel上下文 */
){

    init {
        ctxs.set(this)
    }

    companion object {
        /**
         * 线程安全的rpc上下文对象缓存
         */
        protected val ctxs:ThreadLocal<RpcContext> = ThreadLocal()

        /**
         * 获得当前rpc上下文
         */
        @JvmStatic
        public fun current(): RpcContext {
            return ctxs.get()!!;
        }
    }

}