package com.jksoa.registry

import com.jksoa.client.INotifyListener
import com.jksoa.common.Url

/**
 * 服务发现
 *
 * @ClassserviceName: Registry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
interface IDiscovery {

    /**
     * 监听服务变化
     *
     * @param serviceName 服务名
     * @param listener 监听器
     */
    fun subscribe(serviceName: String, listener: INotifyListener)

    /**
     * 取消监听服务变化
     *
     * @param serviceName 服务名
     * @param listener 监听器
     */
    fun unsubscribe(serviceName: String, listener: INotifyListener)

    /**
     * 发现服务
     *
     * @param serviceName 服务名
     * @return 服务地址
     */
    fun discover(serviceName: String): List<Url>

}