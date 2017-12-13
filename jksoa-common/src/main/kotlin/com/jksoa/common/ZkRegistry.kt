package com.jksoa.common

import com.jkmvc.common.Config
import com.jkmvc.common.Url
import org.I0Itec.zkclient.ZkClient

/**
 * 基于zookeeper的注册中心
 *
 * @ClassName: ZkRegistry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object ZkRegistry :IRegistry{

    /**
     * zk配置
     */
    public val config = Config.instance("zk")

    /**
     * zk客户端
     */
    public val zkClient:ZkClient = ZkClient(config.getString("address")!!, config.getInt("sessionTimeout", 5000)!!, config.getInt("timeout", 5000)!!)


    public override fun register(url: Url) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    public override fun unregister(url: Url) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}