package net.jkcode.jksoa.rpc.registry

import net.jkcode.jksoa.common.Url

/**
 * 服务发现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
interface IDiscovery {

    /**
     * 监听服务变化
     *
     * @param serviceId 服务标识
     * @param listener 监听器
     */
    fun subscribe(serviceId: String, listener: IDiscoveryListener)

    /**
     * 取消监听服务变化
     *
     * @param serviceId 服务标识
     * @param listener 监听器
     */
    fun unsubscribe(serviceId: String, listener: IDiscoveryListener)

    /**
     * 发现服务
     *
     * @param serviceId 服务标识
     * @return 服务地址
     */
    fun discover(serviceId: String): List<Url>

}