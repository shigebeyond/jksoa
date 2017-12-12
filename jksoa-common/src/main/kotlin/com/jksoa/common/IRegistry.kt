package com.jksoa.common

import com.jkmvc.common.URL


/**
 * 注册中心
 *
 * @ClassserviceName: Registry
 * @Description: 
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
interface IRegistry {

    companion object{

        /**
         * 获得指定类型的注册中心
         *
         * @param type
         * @return
         */
        public fun instance(type: String): IRegistry{

        }
    }

    /**
     * 注册服务
     *
     * @param serviceName
     * @param url
     * @return
     */
    fun register(serviceName: String, url: URL)

    /**
     * 注销服务
     *
     * @param serviceName
     * @param url
     * @return
     */
    fun unregister(serviceName: String, url: URL)

    fun addConnectedListener(listener:(Boolean) -> Unit)

    fun addChildListener(listener:(Boolean) -> Unit)

}