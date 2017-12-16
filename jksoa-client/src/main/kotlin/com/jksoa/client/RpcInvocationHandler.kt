package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.serialize.ISerializer
import com.jksoa.common.IService
import com.jksoa.common.Request
import com.jksoa.common.clientLogger
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

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
     * 处理方法调用: 调用 Broker
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args 参数
     */
    public override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
        clientLogger.debug("RpcInvocationHandler调用远端方法: " + `interface`.name + '.' + method.name + '(' + args.joinToString() + ')')
        // 封装请求
        val req = Request(`interface`, method, args)

        try {
            // 发送调用请求，并返回结果
            // 1 同步调用
            val res = Broker.call(req)
            return res.value
        }catch (e: Exception){
            throw RpcException("rpc调用错误：" + e.message, e)
        }
    }

}