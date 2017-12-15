package com.jksoa.tests.rmi

import java.rmi.server.UnicastRemoteObject

/**
 * rmi协议服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class HelloService : UnicastRemoteObject(), IHelloService {

    override fun sayHello(name: String): String {
        return "Hello, $name, Welcome to China, Let me introduce you 'JKSOA' framework."
    }

}