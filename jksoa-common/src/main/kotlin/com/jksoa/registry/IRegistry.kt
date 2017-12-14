package com.jksoa.registry

import com.jksoa.registry.IDiscovery
import com.jksoa.common.Url


/**
 * 注册中心
 *
 * @ClassserviceName: Registry
 * @Description: 
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
interface IRegistry: IDiscovery {

    /**
     * 注册服务
     *
     * @param url
     * @return
     */
    fun register(url: Url)

    /**
     * 注销服务
     *
     * @param url
     * @return
     */
    fun unregister(url: Url)
}