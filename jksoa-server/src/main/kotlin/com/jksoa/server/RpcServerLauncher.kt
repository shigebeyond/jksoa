package net.jkcode.jksoa.server

import com.jkmvc.common.Config

/**
 * 服务器启动
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-16.
 */
object RpcServerLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // 获得服务端配置
        val config = Config.instance("server", "yaml")
        // 获得指定的协议的服务实例
        val protocol: String = config["protocol"]!!
        val server = IRpcServer.instance(protocol)
        // 启动服务
        server.start()
    }

}
