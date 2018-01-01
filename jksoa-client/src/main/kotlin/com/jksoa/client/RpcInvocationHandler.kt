package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.serialize.ISerializer
import com.jksoa.common.IService
import com.jksoa.common.Request
import com.jksoa.common.clientLogger
import com.jksoa.common.future.ResultFuture
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.Future

/**
 * rpc调用的代理实现
 *
 * @ClassName: WebServiceInvoker
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
class RpcInvocationHandler(public val `interface`: Class<out IService> /* 接口类 */): InvocationHandler {

    companion object{

        /**
         * 异步方法的后缀
         */
        public val ASYNC_METHOD_SUFFIX = "Async"

        /**
         * 创建服务代理
         */
        public fun createProxy(intf: Class<out IService>): IService {
            return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler(intf)) as IService
        }
    }

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 序列化
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializeType"]!!)

    /**
     * 远程服务中转器
     */
    public val broker: IBroker = Broker

    /**
     * 处理方法调用: 调用 Broker
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args 参数
     */
    public override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
        clientLogger.debug("RpcInvocationHandler调用远端方法: " + `interface`.name + '.' + method.name + '(' + args.joinToString() + ')')

        // 1 检查是否异步方法
        var async = isAsyncMethod(method)

        // 2 封装请求
        val req = Request(`interface`, getMethodSignature(method, async), args)

        // 3 发送请求，并获得延后的响应
        val resFuture = broker.call(req)

        // 4 返回结果
        if(async) // 异步结果
            return resFuture

        // 同步结果
        return resFuture.get().value
    }

    /**
     * 是否异步方法
     *
     * @param method
     * @return
     */
    private fun isAsyncMethod(method: Method): Boolean {
        return method.name.endsWith(ASYNC_METHOD_SUFFIX) && method.returnType == Future::class.java
    }

    /**
     * 获得方法签名
     *   如果是异步方法，则要去掉 Async 后缀
     *
     * @param method
     * @param async 异步
     * @return
     */
    private fun getMethodSignature(method: Method, async: Boolean): String {
        var methodName = if(async)
                            method.name.removeSuffix(ASYNC_METHOD_SUFFIX)
                        else
                            method.name
        return method.parameterTypes.joinTo(StringBuilder(methodName), ",", "(", ")"){
            it.name
        }.toString().replace("java.lang.", "")
    }

}