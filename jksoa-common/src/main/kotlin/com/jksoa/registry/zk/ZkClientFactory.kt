package com.jksoa.registry.zk

import com.jkmvc.common.Config
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

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
    private val clients: ConcurrentHashMap<String, ZkClient> = ConcurrentHashMap()

    /**
     * 构建zk客户端
     *
     * @param name
     * @return
     */
    private fun buildClient(name: String): ZkClient {
        // 获得zk配置
        val config = Config.instance("zk.${name}", "yaml")

        // 创建zk客户端
        return ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("connectionTimeout", 5000)!!)
    }
    /**
     * 获得zk连接
     *
     * @param name
     * @return
     */
    public fun instance(name: String = "default"): ZkClient {
        return clients.getOrPut(name){
            buildClient(name)
        }
    }
}