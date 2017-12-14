package com.jksoa.registry.zk

import com.jkmvc.common.Config
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
     * zk配置
     */
    private val config = Config.instance("zk")

    /**
     * zk客户端
     */
    private val zkClient: ZkClient = ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("connectionTimeout", 5000)!!)

    /**
     * 获得zk连接
     *
     * @return
     */
    public fun instance(): ZkClient {
        return zkClient
    }
}