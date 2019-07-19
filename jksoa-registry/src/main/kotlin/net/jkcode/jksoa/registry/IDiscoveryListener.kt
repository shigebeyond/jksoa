package net.jkcode.jksoa.registry

import net.jkcode.jksoa.common.Url

/**
 * 服务发现的监听器：监听某个服务的地址变化
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
interface IDiscoveryListener {

    /**
     * 服务标识，即接口类全名
     */
    val serviceId: String

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>)

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    fun handleServiceUrlRemove(serverName: String, allUrls: Collection<Url>)

    /**
     * 处理服务配置参数（服务地址的参数）变化
     * @param url
     * @param allUrls
     */
    fun handleParametersChange(url: Url, allUrls: Collection<Url>): Unit
}