package com.jkmvc.szpower.util

import com.jkmvc.common.Config
import com.jkmvc.serialize.ISerializer
import com.jksoa.common.Broker
import com.jksoa.common.IService
import com.jksoa.common.Request
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
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        // 封装请求
        val req = Request(`interface`, method, args)

        try {
            // 发送调用请求，并返回结果
            val res = Broker.call(req)
        }catch (e: Exception){

        }

        return null
    }

}