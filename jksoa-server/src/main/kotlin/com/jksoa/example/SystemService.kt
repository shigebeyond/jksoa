package com.jksoa.example

import com.jkmvc.common.Config
import com.jkmvc.common.ServiceMeta
import com.jkmvc.common.format
import com.jkmvc.common.randomLong
import com.jksoa.common.RpcRequest
import com.jksoa.common.serverLogger
import java.rmi.RemoteException
import java.util.*

/**
 * 系统服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
@ServiceMeta(leader = true)
class SystemService : ISystemService /*, UnicastRemoteObject() // rmi协议服务实现*/{

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
        val config = Config.instance("client", "yaml")
        val millis: Long = randomLong(config["requestTimeoutMillis"]!!) * 2
        serverLogger.debug("睡 $millis ms")
        Thread.sleep(millis)
        return millis
    }

    /**
     * 检查客户端的接口版本, 用于版本兼容
     */
    @Throws(RemoteException::class) // rim异常
    public override fun checkVersion(){
        val v = RpcRequest.current().version
        println("version=$v")
    }
}