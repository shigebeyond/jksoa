package net.jkcode.jksoa.rpc.client.invoker

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInvoker
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler

/**
 * rpc请求的方法调用器
 *    只是方便接口 IInvocation.invoke() 统一调用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-15 5:58 PM
 */
class RpcRequestInvoker: IRpcRequestInvoker {

    /**
     * 调用
     * @param req
     * @return
     */
    public override fun invoke(req: IRpcRequest): Any?{
        //return IRpcRequestDispatcher.instance().dispatch(req) // 无拦截器链
        return RpcInvocationHandler.invokeRpcRequest(req) // 有拦截器链
    }
}