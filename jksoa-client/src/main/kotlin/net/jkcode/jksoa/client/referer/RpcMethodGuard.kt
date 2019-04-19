package net.jkcode.jksoa.client.referer

import net.jkcode.jksoa.common.getServiceClass
import net.jkcode.jksoa.guard.MethodGuard
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 2:22 PM
 */
class RpcMethodGuard(method: Method): MethodGuard(method) {

    /**
     * 调用合并后的方法
     *   因为 MethodGuard 自身是通过方法反射来调用的, 因此不能再直接反射调用 method.invoke(obj, arg), 否则会递归调用以致于死循环
     *
     * @param singleArg
     * @return
     */
    public override fun invokeCombineMethod(singleArg: Any?):Any?{
        val obj = Referer.getRefer(method.getServiceClass())
        //return method.invoke(obj, singleArg)
        return RpcInvocationHandler.doInvoke(method, obj, arrayOf(singleArg))
    }

    companion object {

        /**
         * 方法守护者
         */
        protected val methodGuards: ConcurrentHashMap<Method, MethodGuard> = ConcurrentHashMap();

        /**
         * 获得方法守护者
         * @param method
         * @return
         */
        public fun instance(method: Method): MethodGuard{
            return methodGuards.getOrPut(method){
                RpcMethodGuard(method)
            }
        }

        /**
         * 获得方法守护者
         * @param func
         * @return
         */
        public fun instance(func: KFunction<*>): MethodGuard{
            return instance(func.javaMethod!!)
        }
    }
}