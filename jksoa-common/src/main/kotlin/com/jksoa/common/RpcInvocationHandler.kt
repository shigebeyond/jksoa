package com.jkmvc.szpower.util

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
class RpcInvocationHandler(): InvocationHandler {

    companion object{

        /**
         * 创建代理的工厂方法
         *
         * @param interfaces 要代理的接口
         * @return
         */
        public fun createProxy(vararg interfaces:Class<*>): Any? {
            // 获得类加载器
            val cld = Thread.currentThread().contextClassLoader

            // 创建代理
            return Proxy.newProxyInstance(cld, interfaces, RpcInvocationHandler());
        }
    }

    /**
     * 处理方法调用
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args 参数
     */
    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        // 封装请求

        // 发送调用请求

        // 等待，并处理结果

        return null
    }

}