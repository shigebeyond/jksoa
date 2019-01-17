package com.jksoa.example

import com.jkmvc.common.randomLong
import com.jksoa.common.serverLogger
import java.rmi.RemoteException

/**
 * ping服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class PingService : IPingService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun ping(): String {
        val timeout = randomLong(1000)
        serverLogger.debug("睡 $timeout ms")
        Thread.sleep(timeout)
        return "pong"
    }

}