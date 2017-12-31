package com.jksoa.example

import com.jksoa.common.IService
import java.rmi.Remote
import java.rmi.RemoteException

/**
 * rmi协议服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
interface IEchoService : Remote, IService {

    @Throws(RemoteException::class)
    fun echo(msg: String): String

    fun echoAsync(msg: String): String{
        throw Exception()
    }
}