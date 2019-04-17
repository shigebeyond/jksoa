package net.jkcode.jksoa.client.combiner

import net.jkcode.jkmvc.combiner.GroupFutureSupplierCombiner
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jkmvc.common.isSuperClass
import net.jkcode.jksoa.client.combiner.annotation.GroupCombine
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.getServiceClass
import net.jkcode.jksoa.common.groupCombine
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction2
import kotlin.reflect.jvm.javaMethod

/**
 * 针对每个group的取值操作合并, 每个group攒够一定数量/时间的请求才执行
 *    如请求合并/cache合并等
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-10 9:47 AM
 */
class GroupRpcRequestCombiner<RequestArgumentType /* 请求参数类型 */, ResponseType /* 响应类型 */, BatchItemType: Any /* 批量取值操作的返回列表的元素类型 */>(
        reqArgField: String, /* 请求参数对应的响应字段名 */
        respField: String = "", /* 要返回的响应字段名, 如果为空则取响应对象 */
        one2one: Boolean = true, /* 请求对响应是一对一(ResponseType是非List), 还是一对多(ResponseType是List) */
        flushSize: Int = 100 /* 触发刷盘的队列大小 */,
        flushTimeoutMillis: Long = 100 /* 触发刷盘的定时时间 */,
        batchFutureSupplier:(List<RequestArgumentType>) -> CompletableFuture<List<BatchItemType>> /* 批量取值操作 */
): GroupFutureSupplierCombiner<RequestArgumentType, ResponseType, BatchItemType>(reqArgField, respField, one2one, flushSize, flushTimeoutMillis, batchFutureSupplier){

    public constructor(batchMethod: Method, reqArgField: String, respField: String = "", one2one: Boolean = true, flushSize: Int = 100, flushTimeoutMillis: Long = 100)
            :this(reqArgField, respField, one2one, flushSize, flushTimeoutMillis, dispatcher.method2DispatchLambda(batchMethod))

    public constructor(batchFunc: KFunction2<*, List<RequestArgumentType>, List<ResponseType>>, reqArgField: String, respField: String = "", one2one: Boolean = true, flushSize: Int = 100, flushTimeoutMillis: Long = 100)
            :this(reqArgField, respField, one2one, flushSize, flushTimeoutMillis, dispatcher.method2DispatchLambda(batchFunc.javaMethod!!))

    public constructor(batchMethod: Method, annotation: GroupCombine):this(batchMethod, annotation.reqArgField, annotation.respField, annotation.one2one, annotation.flushSize, annotation.flushTimeoutMillis)

    companion object {
        /**
         * 请求分发者
         */
        protected val dispatcher: IRpcRequestDispatcher = RcpRequestDispatcher

        /**
         * 缓存方法的rpc请求合并器: <方法 to GroupRpcRequestCombiner>
         */
        protected val groupCombiners: ConcurrentHashMap<Method, GroupRpcRequestCombiner<Any, Any?, Any>> = ConcurrentHashMap();

        /**
         * 获得方法对应的MethodHandle对象
         * @param method
         * @return
         */
        public fun instance(method: Method): GroupRpcRequestCombiner<Any, Any?, Any> {
            return groupCombiners.getOrPut(method){
                val annotation = method.groupCombine!!
                // 找到批量操作的方法
                val batchMethod = method.declaringClass.methods.first { it.name == annotation.batchMethod }
                val msg = "方法[${method.getSignature()}]的注解@GroupCombine中声明的batchMethod=[${annotation.batchMethod}]"
                if(batchMethod == null)
                    throw RpcClientException("${msg}不存在")
                // 检查方法参数
                val pt = batchMethod.parameterTypes
                if(pt.size != 1 || !List::class.java.isAssignableFrom(pt.first()))
                    throw RpcClientException("${msg}必须有唯一的List类型的参数")
                // 检查方法返回值
                if(!List::class.java.isAssignableFrom(batchMethod.returnType) && !CompletableFuture::class.java.isAssignableFrom(batchMethod.returnType))
                    throw RpcClientException("${msg}必须有的List或CompletableFuture<List>类型的返回值")
                // 创建请求合并器
                GroupRpcRequestCombiner(batchMethod, annotation)
            }
        }
    }


}