package com.jksoa.common

import com.jksoa.common.IService

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
    public abstract fun getRefer(clazz: Class<out IService>): IService

    /**
     * 获得服务代理
     *
     * @return
     */
    public inline fun <reified T: IService> getRefer(): T {
        return Referer.getRefer(T::class.java) as T
    }
}