package com.jksoa.registry.zk.common

import org.I0Itec.zkclient.ZkClient

/**
 * zk连接工厂
 *
 * @ClassName: ZkClientFactory
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object ZkClientFactory{

    /**
     * 缓存zk客户端
     */
    private val clients: java.util.concurrent.ConcurrentHashMap<String, ZkClient> = java.util.concurrent.ConcurrentHashMap()

    /**
     * 构建zk客户端
     *
     * @param name
     * @return
     */
    private fun buildClient(name: String): org.I0Itec.zkclient.ZkClient {
        // 获得zk配置
        val config = com.jkmvc.common.Config.Companion.instance("zk.${name}", "yaml")

        // 创建zk客户端
        return org.I0Itec.zkclient.ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("connectionTimeout", 5000)!!)
    }
    /**
     * 获得zk连接
     *
     * @param name
     * @return
     */
    public fun instance(name: String = "default"): org.I0Itec.zkclient.ZkClient {
        return com.jksoa.registry.zk.common.ZkClientFactory.clients.getOrPut(name){
            com.jksoa.registry.zk.common.ZkClientFactory.buildClient(name)
        }
    }
}