package net.jkcode.jksoa.registry

import net.jkcode.jksoa.registry.IDiscovery
import net.jkcode.jksoa.common.Url


/**
 * 注册中心
 *
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