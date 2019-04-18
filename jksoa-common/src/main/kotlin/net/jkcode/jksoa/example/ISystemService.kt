package net.jkcode.jksoa.example

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.common.annotation.ServiceMethodMeta
import java.rmi.RemoteException

/**
 * 系统服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@Service(version = 1, onlyLeader = true)
interface ISystemService : IService /*, Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun ping(): String

    @Throws(RemoteException::class) // rmi异常
    fun echo(msg: String = "test"): String

    @Throws(RemoteException::class) // rim异常
    fun sleep(): Long

    @Throws(RemoteException::class) // rim异常
    fun checkVersion()

    @Throws(RemoteException::class) // rim异常
    @JvmDefault
    fun defaultMethod(msg:String){
        println("call default method, with parameter: $msg")
    }

}