package com.jksoa.common

import com.jkmvc.szpower.util.RpcInvocationHandler
import com.jksoa.common.IService
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * 服务的引用（代理）
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
object Referer: IReferer() {

    /**
     * 服务引用缓存
     */
    private val refers = ConcurrentHashMap<Class<*>, IService>()

    /**
     * 创建服务引用
     *
     * @param clazz
     * @return
     */
    private fun createRefer(clazz: Class<out IService>): IService {
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), RpcInvocationHandler(clazz)) as IService
    }

    /**
     * 添加服务引用
     *   主要是本地服务提供者调用，添加本地服务
     *
     * @param clazz
     * @param refer
     * @return
     */
    public override fun addRefer(clazz: Class<out IService>, refer: IService): Unit{
        refers[clazz] = refer
    }

    /**
     * 根据服务接口，来获得服务引用
     *
     * @param clazz
     * @return
     */
    public override fun <T: IService> getRefer(clazz: Class<T>): T {
        return refers.getOrPut(clazz){
            createRefer(clazz)
        } as T
    }

}