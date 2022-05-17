package net.jkcode.jksoa.rpc.example

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jksoa.common.annotation.RemoteService
import java.rmi.RemoteException

/**
 * 简单示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@RemoteService(version = 1, onlyLeader = true)
interface ISimpleService /*: Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    @Suspendable
    fun hostname(): String

    @Throws(RemoteException::class) // rmi异常
    fun sayHi(name: String = "shi"): String

    @Throws(RemoteException::class) // rim异常
    fun checkVersion()

    @Throws(RemoteException::class) // rim异常
    @JvmDefault
    fun defaultMethod(msg:String){
        println("call default method, with parameter: $msg")
    }

    /**
     * 抛个异常
     */
    @Throws(RemoteException::class) // rim异常
    fun testException()

}