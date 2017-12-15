package com.jksoa.server

import com.jksoa.common.IService
import java.lang.reflect.Method

/**
 * 服务提供者
 *
 * @ClassName: Provider
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
interface IProvider {

    /**
     * 接口类
     */
    val interfaces: MutableList<Class<out IService>>

    /**
     * 实现类
     */
    val clazz: Class<out IService>

    /**
     * 所有方法
     */
    val methods: MutableMap<String, Method>

    /**
     * 服务实例
     */
    var service: IService

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    fun getMethod(methodSignature: String): Method?

    /**
     * 注册服务
     */
    fun registerService()

    /**
     * 注册本地服务引用
     *   对要调用的服务，如果本地有提供，则直接调用本地的服务
     */
    fun registerLocalRefer()

    /**
     * 代理服务来执行方法
     *
     * @param method
     * @param args
     * @return
     */
    fun call(method: Method, args: Array<Any>): Any?
}