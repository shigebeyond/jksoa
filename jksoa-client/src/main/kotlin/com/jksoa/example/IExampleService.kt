package com.jksoa.example

import com.jksoa.common.IService
import java.rmi.Remote
import java.rmi.RemoteException
import java.util.concurrent.Future

/**
 * 示例服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
interface IExampleService : IService /*, Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun sayHi(name: String): String

    fun sumRange(start: Int, endInclusive: Int): Int
}