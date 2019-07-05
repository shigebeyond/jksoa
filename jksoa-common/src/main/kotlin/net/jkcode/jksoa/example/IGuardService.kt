package net.jkcode.jksoa.example

import net.jkcode.jksoa.client.combiner.annotation.*
import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.guard.circuit.CircuitBreakType
import java.io.Serializable
import java.rmi.RemoteException
import java.util.concurrent.CompletableFuture

data class User(public val id: Int, public val name: String): Serializable {}

/**
 * 守护者示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@Service(version = 1)
interface IGuardService : IService /*, Remote // rmi协议服务接口 */ {

    // 默认方法
    @JvmDefault
    fun getUserById(id: Int): User{
        return getUserByIdAsync(id).get()
    }

    // key合并
    @KeyCombine
    fun getUserByIdAsync(id: Int): CompletableFuture<User>

    // 默认方法
    @JvmDefault
    fun getUserByName(name: String): User{
        return getUserByNameAsync(name).get()
    }

    // group合并
    @GroupCombine("listUsersByNameAsync", "name", "", true, 100, 100)
    fun getUserByNameAsync(name: String): CompletableFuture<User>

    // 默认方法
    @JvmDefault
    fun listUsersByName(names: List<String>): List<User>{
        return listUsersByNameAsync(names).get()
    }

    // group合并后要调用的批量方法
    fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<User>>

    // 降级: 有异常后备方法
    @Degrade(fallbackMethod = "getUserWhenFallback")
    // 断路器
    @CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
    fun getUserWhenException(id: Int): User

    // 发送异常时调用的方法, 一般是默认方法
    @JvmDefault
    fun getUserWhenFallback(id: Int): User {
        return User(-1, "无名氏")
    }

    // 统计请求数
    @Metric()
    // 断路器
    @CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
    fun getUserWhenRandomException(id: Int): User
}