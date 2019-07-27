package net.jkcode.jksoa.example

import net.jkcode.jkmvc.common.randomLong
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.RpcServerContext
import java.rmi.RemoteException

/**
 * 简单示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class SimpleService : ISimpleService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun ping(): String {
        serverLogger.debug("收到ping请求, 响应pong")
        return "pong"
    }

    @Throws(RemoteException::class) // rim异常
    public override fun echo(msg: String): String{
        serverLogger.debug("收到echo请求: $msg")
        return msg
    }

    @Throws(RemoteException::class) // rim异常
    public override fun sleep(): Long {
        val millis: Long = randomLong(500) * 2
        serverLogger.debug("睡 $millis ms")
        Thread.sleep(millis)
        return millis
    }

    /**
     * 检查客户端的接口版本, 用于版本兼容
     */
    @Throws(RemoteException::class) // rim异常
    public override fun checkVersion(){
        val v = RpcServerContext.currentRequest().version
        println("version=$v")
    }
}