package net.jkcode.jksoa.dtx.demo.order.filter

import net.jkcode.jkmvc.http.JkFilter
import net.jkcode.jksoa.rpc.server.IRpcServer
import javax.servlet.FilterConfig

/**
 * web入口
 *
 * @author shijianhang
 * @date 2019-4-13 上午9:27:56
 */
class MyFilter() : JkFilter() {

    override fun init(filterConfig: FilterConfig) {
        super.init(filterConfig)

        // 启动rpc server
        val server = IRpcServer.instance("netty")
        server.start(false)
    }

}
