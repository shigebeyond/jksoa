package net.jkcode.jksoa.example

import net.jkcode.jkmvc.common.randomString
import java.rmi.RemoteException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * 示例服务实现
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class ExampleService : IExampleService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    private val userCounter = AtomicInteger(0)

    @Throws(RemoteException::class) // rim异常
    public override fun sayHi(name: String): String {
        return "Hi, $name"
    }

    /**
     * 根据id获得user
     *   单个参数的处理方法
     */
    public override fun getUserByIdAsync(id: Int): CompletableFuture<User> {
        println("测试调用 CombineService.getUserById() 实现, 只执行一次")
        val u = User(id, randomString(7))
        return CompletableFuture.completedFuture(u)
    }

    /**
     * 根据name获得user
     */
    public override fun getUserByNameAsync(name: String): CompletableFuture<User> {
        val u = User(userCounter.incrementAndGet(), name)
        return CompletableFuture.completedFuture(u)
    }

    /**
     * 根据一组name获得user
     *    一组参数的批量处理方法
     */
    public override fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<User>>{
        println("测试调用 CombineService.listUsersByName() 实现, 只执行一次")
        var i = 0
        val us = names.map {name ->
            getUserByName(name)
        }
        return CompletableFuture.completedFuture(us)
    }


}