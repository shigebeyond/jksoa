package com.jksoa.common

import com.jkmvc.szpower.util.RpcInvocationHandler
import com.jksoa.common.IService
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 * 服务代理的引用
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
object Referer: IReferer() {

    /**
     * 服务代理缓存
     */
    private val refers = ConcurrentHashMap<Class<*>, IService>()

    /**
     * 创建服务代理
     *
     * @param clazz
     * @return
     */
    private fun createRefer(clazz: Class<out IService>): IService {
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), RpcInvocationHandler) as IService
    }

    /**
     * 获得服务代理
     *
     * @param clazz
     * @return
     */
    public override fun getRefer(clazz: Class<out IService>): IService {
        return refers.getOrPut(clazz){
            createRefer(clazz)
        }
    }

}