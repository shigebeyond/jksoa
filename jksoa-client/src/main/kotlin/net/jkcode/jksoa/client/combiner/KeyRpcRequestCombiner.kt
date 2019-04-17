package net.jkcode.jksoa.client.combiner

import net.jkcode.jkmvc.combiner.KeyFutureSupplierCombiner
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction2
import kotlin.reflect.jvm.javaMethod

/**
 * 针对每个参数值的rpc请求合并
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-16 7:18 PM
 */
class KeyRpcRequestCombiner<RequestArgumentType /* 请求参数类型 */, ResponseType /* 响应类型 */>(futureSupplier: (RequestArgumentType) -> CompletableFuture<ResponseType>)
    : KeyFutureSupplierCombiner<RequestArgumentType, ResponseType>(futureSupplier) {

    public constructor(method: Method):this(dispatcher.method2DispatchLambda(method))

    public constructor(func: KFunction2<*, RequestArgumentType, ResponseType>):this(dispatcher.method2DispatchLambda(func.javaMethod!!))

    companion object {
        /**
         * 请求分发者
         */
        protected val dispatcher: IRpcRequestDispatcher = RcpRequestDispatcher

        /**
         * 缓存方法的rpc请求合并器: <方法 to KeyRpcRequestCombiner>
         */
        protected val keyCombiners: ConcurrentHashMap<Method, KeyRpcRequestCombiner<Any, Any?>> = ConcurrentHashMap();

        /**
         * 获得方法对应的MethodHandle对象
         * @param method
         * @return
         */
        public fun instance(method: Method): KeyRpcRequestCombiner<Any, Any?> {
            return keyCombiners.getOrPut(method){
                KeyRpcRequestCombiner(method)
            }
        }
    }
}
