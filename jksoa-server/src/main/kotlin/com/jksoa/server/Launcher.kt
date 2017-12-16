package com.jksoa.server

import com.jkmvc.common.Config
import com.jksoa.common.serverLogger
import com.jksoa.protocol.IProtocolServer

/**
 * 启动器
 *
 * @ClassName: Server
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-16.
 */
object Launcher {

    @JvmStatic
    fun main(args: Array<String>) {
        // 获得服务端配置
        val config = Config.instance("server", "yaml")
        // 获得指定的协议的服务实例
        val protocol: String = config["protocol"]!!
        val server = IProtocolServer.instance(protocol)
        // 启动服务
        server.start()
        serverLogger.info("Launcher启动[$protocol]协议服务")
    }

}
