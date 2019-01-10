package com.jksoa.example

import java.rmi.RemoteException

/**
 * 示例服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class ExampleService : IExampleService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    @Throws(RemoteException::class) // rim异常
    public override fun sayHi(name: String): String {
        return "Hi, $name"
    }

    public override fun sumRange(start: Int, endInclusive: Int): Int {
        var s = 0
        for(i in (start until endInclusive))
            s += i
        return s
    }

}