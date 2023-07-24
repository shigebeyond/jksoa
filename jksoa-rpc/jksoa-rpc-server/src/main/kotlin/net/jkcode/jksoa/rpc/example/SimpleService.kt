package net.jkcode.jksoa.rpc.example

import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.rpc.server.RpcServerContext
import net.jkcode.jkutil.common.SysInfo
import java.net.InetAddress
import java.rmi.RemoteException

/**
 * 简单示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class SimpleService : ISimpleService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun hostname(): String {
        return SysInfo.hostname // 在swarm为容器id，在k8s为pod名
    }

    /**
     * 获得pod信息
     */
    @Throws(RemoteException::class) // rim异常
    override fun podInfo(): Map<String, String> {
        return mapOf(
            "POD_NAME" to System.getenv("POD_NAME"), // pod名=hostname
            "POD_NAMESPACE" to System.getenv("POD_NAMESPACE"),
            "POD_IP" to System.getenv("POD_IP")
        )
    }

    @Throws(RemoteException::class) // rim异常
    public override fun sayHi(name: String): String{
        return "Greeting, $name"
    }

    /**
     * 检查客户端的接口版本, 用于版本兼容
     */
    @Throws(RemoteException::class) // rim异常
    public override fun checkVersion(){
        val v = RpcServerContext.currentRequest().version
        serverLogger.debug("version=$v")
    }

    /**
     * 抛个异常
     */
    @Throws(RemoteException::class) // rim异常
    public override fun testException(){
        throw IllegalArgumentException("hello exception")
    }
}