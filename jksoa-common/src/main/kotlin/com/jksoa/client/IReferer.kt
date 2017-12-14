package com.jksoa.common

/**
 * 服务代理的引用
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
abstract class IReferer {

    /**
     * 获得服务代理
     *
     * @param clazz
     * @return
     */
    public abstract fun getProxy(clazz: Class<out IService>): IService

    /**
     * 获得服务代理
     *
     * @return
     */
    public inline fun <reified T: IService> getProxy(): T {
        return getProxy(T::class.java) as T
    }
}