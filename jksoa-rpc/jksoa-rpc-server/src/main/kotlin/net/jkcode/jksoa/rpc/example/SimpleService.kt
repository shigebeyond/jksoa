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
        return SysInfo.hostname // 容器id
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