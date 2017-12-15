package com.jksoa.common

/**
 * 服务的引用（代理）
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
abstract class IReferer {

    /**
     * 添加服务引用
     *    主要是本地服务提供者调用，添加本地服务
     *
     * @param clazz
     * @param refer
     * @return
     */
    public abstract fun addRefer(clazz: Class<out IService>, refer: IService): Unit

    /**
     * 获得服务引用
     *
     * @param clazz
     * @return
     */
    public abstract fun getRefer(clazz: Class<out IService>): IService

    /**
     * 获得服务引用
     *
     * @return
     */
    public inline fun <reified T: IService> getRefer(): T {
        return getRefer(T::class.java) as T
    }
}