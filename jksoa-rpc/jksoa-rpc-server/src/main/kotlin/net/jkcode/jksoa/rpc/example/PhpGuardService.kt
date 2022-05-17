package net.jkcode.jksoa.rpc.example

import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jkutil.common.randomBoolean
import net.jkcode.jkutil.common.randomString
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * 给php调用的守护者示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:39
 **/
class PhpGuardService : IPhpGuardService /*, UnicastRemoteObject() // rmi协议服务实现*/{

    private val userCounter = AtomicInteger(0)

    /**
     * 根据id获得user
     *   单个参数的处理方法
     */
    public override fun getUserByIdAsync(id: Int): CompletableFuture<Map<String, Any?>> {
        serverLogger.debug("测试调用 CombineService.getUserById($id) 实现, 只执行一次")
        val u = buildUser(id, randomString(7))
        return CompletableFuture.completedFuture(u)
    }

    /**
     * 根据name获得user
     */
    public override fun getUserByNameAsync(name: String): CompletableFuture<Map<String, Any?>> {
        val u = buildUser(userCounter.incrementAndGet(), name)
        return CompletableFuture.completedFuture(u)
    }

    /**
     * 根据一组name获得user
     *    一组参数的批量处理方法
     */
    public override fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<Map<String, Any?>>>{
        serverLogger.debug("测试调用 CombineService.listUsersByName(" + names.joinToString() + ") 实现, 只执行一次")
        var i = 0
        val us = names.map {name ->
            buildUser(userCounter.incrementAndGet(), name)
        }
        return CompletableFuture.completedFuture(us)
    }

    /**
     * 根据id获得user -- 直接抛异常
     */
    public override fun getUserWhenException(id: Int): Map<String, Any?> {
        throw Exception("获得用户[$id]发生异常")
    }

    /**
     * 根据id获得user -- 随机抛异常
     */
    public override fun getUserWhenRandomException(id: Int): Map<String, Any?> {
        if(randomBoolean())
            throw Exception("获得用户[$id]发生随机异常")
        return buildUser(id, randomString(7))
    }


}