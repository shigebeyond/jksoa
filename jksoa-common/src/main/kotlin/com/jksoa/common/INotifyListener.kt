package com.jksoa.common

/**
 * 通知服务地址变化
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
interface INotifyListener {

    /**
     * 更新服务地址
     *
     * @param serviceName 服务名
     * @param urls 服务地址
     */
    fun updateServiceUrls(serviceName: String, urls: List<Url>)

    /**
     * 处理新加地址
     *
     * @param url
     */
    fun handleAddUrl(url: Url): Unit

    /**
     * 处理删除地址
     *
     * @param url
     */
    fun handleRemoveUrl(url: Url): Unit

    /**
     * 处理更新地址
     *
     * @param url
     */
    fun handleUpdateUrl(url: Url): Unit
}