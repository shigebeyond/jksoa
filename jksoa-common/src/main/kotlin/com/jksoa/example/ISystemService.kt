package com.jksoa.example

import com.jksoa.common.IService
import java.rmi.Remote
import java.rmi.RemoteException
import java.util.concurrent.Future

/**
 * 系统服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
interface ISystemService : IService /*, Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun ping(): String

    @Throws(RemoteException::class) // rmi异常
    fun echo(msg: String): String

    @Throws(RemoteException::class) // rim异常
    fun sleep(): Long
}