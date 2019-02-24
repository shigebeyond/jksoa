package com.jksoa.example

import com.jksoa.common.IService
import com.jksoa.common.annotation.ServiceMeta
import com.jksoa.common.annotation.ServiceMethodMeta
import java.rmi.RemoteException

/**
 * 系统服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@ServiceMeta(version = 1, onlyLeader = true)
interface ISystemService : IService /*, Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun ping(): String

    @Throws(RemoteException::class) // rmi异常
    fun echo(msg: String = "test"): String

    @Throws(RemoteException::class) // rim异常
    @ServiceMethodMeta(requestTimeoutMillis = 200)
    fun sleep(): Long

    @Throws(RemoteException::class) // rim异常
    fun checkVersion()
}