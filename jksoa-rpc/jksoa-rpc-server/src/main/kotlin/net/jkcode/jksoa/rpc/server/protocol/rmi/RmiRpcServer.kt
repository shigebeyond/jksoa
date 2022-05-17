package net.jkcode.jksoa.rpc.server.protocol.rmi

import net.jkcode.jksoa.rpc.server.IRpcServer
import net.jkcode.jksoa.rpc.server.provider.ProviderLoader
import java.rmi.registry.LocateRegistry
import javax.naming.InitialContext

/**
 * rmi协议-rpc服务器端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiRpcServer : IRpcServer() {

    /**
     * 启动服务器
     */
    public override fun doStart(waitingClose: Boolean, callback: () -> Unit): Unit{
        // 监听端口
        LocateRegistry.createRegistry(serverUrl.port)

        // 初始化命名空间
        val namingContext = InitialContext()
        // 向命名空间注册远程服务实例 => 不需要RpcHandler作为中间转发，直接由rmi自行处理
        for (provider in ProviderLoader.getAll()){
            // 注册url： rmi://192.168.0.106:8081/net.jkcode.jksoa.rpc.example.ISimpleService
            namingContext.rebind(provider.serviceUrl.toString(), provider.service)
        }

        // 调用回调
        callback.invoke()
    }

}