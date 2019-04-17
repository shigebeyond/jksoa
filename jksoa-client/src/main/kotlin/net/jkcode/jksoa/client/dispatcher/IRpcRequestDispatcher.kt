package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jksoa.client.combiner.KeyRpcRequestCombiner
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IShardingRpcRequest
import net.jkcode.jksoa.common.RpcRequest
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * 请求分发者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
interface IRpcRequestDispatcher {

    /**
     * 分发一个请求
     *   将该请求发给任一节点
     *
     * @param req 请求
     * @return 异步结果
     */
    fun dispatch(req: IRpcRequest): CompletableFuture<Any?>

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *
     * @param shdReq 分片的请求
     * @return 多个异步结果
     */
    fun dispatchSharding(shdReq: IShardingRpcRequest): List<CompletableFuture<Any?>>

    /**
     * 将 接口方法的引用 转为 分发请求的lambda
     *    1 方法参数最好是 KFunction1<RequestArgumentType, ResponseType> 有泛型, 而不是 Method 无泛型, 但就算 Method 无泛型, 因为泛型擦除, 也没有报编译错误
     *    2 只支持单参数的方法转换, 主要用在 KeyRpcRequestCombiner / GroupRpcRequestCombiner
     *
     * @param method
     */
    fun <RequestArgumentType, ResponseType> method2DispatchLambda(method: Method):(RequestArgumentType) -> CompletableFuture<ResponseType> {
        return { arg: Any? ->
            dispatch(RpcRequest(method, arrayOf(arg))) as CompletableFuture<ResponseType>
        }
    }
}