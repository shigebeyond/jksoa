package com.jksoa.protocol.rmi

import com.jksoa.protocol.IProtocolServer
import com.jksoa.server.ProviderLoader
import com.jksoa.server.ServiceException
import java.rmi.registry.LocateRegistry
import javax.naming.InitialContext

/**
 * rmi协议-服务器端
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiProtocolServer : IProtocolServer {

    /**
     * 启动服务器
     *
     * @param port 端口
     */
    public override fun doStart(port: Int): Unit{
        try {
            // 监听端口
            LocateRegistry.createRegistry(port)

            // 初始化命名空间
            val namingContext = InitialContext()
            // 向命名空间注册远程服务实例 => 不需要RpcHandler作为中间转发，直接由rmi自行处理
            for (provider in ProviderLoader.getAll()){
                // 注册url： rmi://192.168.0.106:8081/com.jksoa.example.IEchoService
                namingContext.rebind(provider.serviceUrl.toString(), provider.service)
            }
        } catch (e: Exception) {
            throw ServiceException("启动rmi服务失败: " + e.message)
        }
    }

}