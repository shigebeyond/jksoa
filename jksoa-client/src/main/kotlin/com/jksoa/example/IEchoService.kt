package com.jksoa.example

import com.jksoa.common.IService
import java.rmi.Remote
import java.rmi.RemoteException
import java.util.concurrent.Future

/**
 * rmi协议服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
interface IEchoService : IService /*, Remote // rmi */ {

    @Throws(RemoteException::class)
    fun echo(msg: String): String

    fun echoAsync(msg: String): Future<String> {
        throw Exception("异步方法，jksoa框架客户端部分自动实现")
    }
}