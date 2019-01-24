package com.jksoa.server.protocol.rmi

import com.jksoa.server.IRpcServer
import com.jksoa.server.provider.ProviderLoader
import java.rmi.registry.LocateRegistry
import javax.naming.InitialContext

/**
 * rmi协议-服务器端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiServer : IRpcServer() {

    /**
     * 启动服务器
     *   必须在启动后，主动调用 ProviderLoader.load() 来加载服务
     */
    public override fun doStart(): Unit{
        // 监听端口
        LocateRegistry.createRegistry(serverUrl.port)

        // 初始化命名空间
        val namingContext = InitialContext()
        // 向命名空间注册远程服务实例 => 不需要RpcHandler作为中间转发，直接由rmi自行处理
        for (provider in ProviderLoader.getAll()){
            // 注册url： rmi://192.168.0.106:8081/com.jksoa.example.IEchoService
            namingContext.rebind(provider.serviceUrl.toString(), provider.service)
        }

        // 加载服务
        ProviderLoader.load()
    }

    public override fun close() {
    }

}