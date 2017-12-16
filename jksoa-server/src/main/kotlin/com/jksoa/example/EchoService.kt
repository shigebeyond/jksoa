package com.jksoa.example

import java.rmi.RemoteException
import java.rmi.server.UnicastRemoteObject

/**
 * rmi协议服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class EchoService : UnicastRemoteObject(), IEchoService {

    @Throws(RemoteException::class)
    override fun echo(msg: String): String {
        return msg;
    }

}