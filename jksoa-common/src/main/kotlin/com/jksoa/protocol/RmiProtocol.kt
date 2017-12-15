package com.jksoa.protocol

import com.jkmvc.common.Config
import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.Url
import com.jksoa.server.ProviderLoader
import com.jksoa.server.ServiceException
import java.rmi.registry.LocateRegistry
import javax.naming.InitialContext


/**
 * rpc协议
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
object RmiProtocol : IProtocol {

    /**
     * 服务端配置
     */
    private val config = Config.instance("server", "yaml")

    /**
     * 启动服务器
     */
    public override fun startServer(): Unit{
        try {
            // 监听端口
            LocateRegistry.createRegistry(config["port"]!!)

            // 初始化命名空间
            val namingContext = InitialContext()
            // 向命名空间注册远程服务实例
            for (provider in ProviderLoader.getProviders()){
                // 注册url： rmi://localhost:1099/com.jksoa.test.HelloService
                namingContext.rebind(provider.serviceUrl.toString(), provider.service)
            }
        } catch (e: Exception) {
            throw ServiceException("启动rmi服务失败: " + e.message)
        }
    }

    /**
     * 发送客户端请求
     *
     * @param url
     * @param req
     * @return
     */
    public fun sendClientRequest(url: Url, req: Request): Response {
        try {
            // 初始化命名空间
            val namingContext = InitialContext()
            val serv = namingContext.lookup(url.toString(false))
        } catch (e: Exception) {
            throw ServiceException("客户端调用rmi服务失败: " + e.message)
        }
    }
}