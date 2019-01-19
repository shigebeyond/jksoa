package com.jksoa.example

import com.jkmvc.common.format
import com.jksoa.common.serverLogger
import java.rmi.RemoteException
import java.util.*

/**
 * 系统服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class SystemService : ISystemService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun ping(): String {
        return "pong"
    }

    @Throws(RemoteException::class) // rim异常
    public override fun echo(msg: String): String{
        return msg
    }

    @Throws(RemoteException::class) // rim异常
    public override fun sleep(millis: Long): String {
        serverLogger.debug("睡 $millis ms")
        Thread.sleep(millis)
        return Date().format()
    }
}