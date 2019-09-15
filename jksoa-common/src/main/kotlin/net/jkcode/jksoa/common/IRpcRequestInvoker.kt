package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import java.util.concurrent.CompletableFuture

/**
 * rpc请求的方法调用器
 *   只是方便接口 IInvocation.invoke() 统一调用
 *   没有依赖 IRpcRequestDispatcher/RpcInvocationHandler类所在的rpc-client工程, 不能直接调用, 只能通过中间类+反射来解耦依赖
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-15 5:58 PM
 */
interface IRpcRequestInvoker {

    /**
     * 调用
     * @param req
     * @return
     */
    fun invoke(req: IRpcRequest): CompletableFuture<Any?>
}