package net.jkcode.jksoa.rpc.example

import net.jkcode.jkguard.annotation.*
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jkguard.circuit.CircuitBreakType
import java.io.Serializable
import java.util.concurrent.CompletableFuture

/**
 * 给php调用的守护者示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@RemoteService(version = 1)
interface IPhpGuardService /*: Remote // rmi协议服务接口 */ {

    // key合并
    @KeyCombine
    fun getUserByIdAsync(id: Int): CompletableFuture<Map<String, Any?>>

    // group合并
    @GroupCombine("listUsersByNameAsync", "name", "", true, 100, 100)
    fun getUserByNameAsync(name: String): CompletableFuture<Map<String, Any?>>

    // group合并后要调用的批量方法
    // 限流
    @RateLimit(100.0)
    fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<Map<String, Any?>>>

    // 降级: 有异常后备方法
    @Degrade(fallbackMethod = "getUserWhenFallback")
    fun getUserWhenException(id: Int): Map<String, Any?>

    // 发送异常时调用的方法, 一般是默认方法
    @JvmDefault
    fun getUserWhenFallback(id: Int): Map<String, Any?> {
        return buildUser(-1, "无名氏")
    }

    // 统计请求数
    @Metric()
    // 断路器
    @CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
    fun getUserWhenRandomException(id: Int): Map<String, Any?>
}

fun buildUser(id: Int, name: String): Map<String, Any> {
    return mapOf("id" to id, "name" to name)
}