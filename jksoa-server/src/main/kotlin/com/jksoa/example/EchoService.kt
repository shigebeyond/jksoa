package com.jksoa.example

import java.rmi.RemoteException

/**
 * 回显服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class EchoService : IEchoService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun echo(msg: String): String {
        return msg
    }

}