package com.jksoa.server

import com.jkmvc.common.Config
import com.jksoa.protocol.IProtocolServer

/**
 * 服务器
 *
 * @ClassName: Server
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-16.
 */
object Server {

    @JvmStatic
    fun main(args: Array<String>) {
        // 获得服务端配置
        val config = Config.instance("server", "yaml")
        // 获得指定的协议的服务实例
        val pro = IProtocolServer.instance(config["protocol"]!!)
        // 启动服务
        pro.start()
    }

}
