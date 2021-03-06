package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkutil.common.makeThreads
import net.jkcode.jkutil.common.print
import net.jkcode.jkutil.common.randomString
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.IGuardService
import net.jkcode.jksoa.rpc.example.User
import net.jkcode.jkguard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jkguard.combiner.KeyFutureSupplierCombiner
import org.junit.Test
import java.util.concurrent.CompletableFuture

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RpcClientMethodGuardTests {

    val service = Referer.getRefer<IGuardService>()

    /************************* 测试key合并 **************************/
    /**
     * 测试key合并 -- 手动调用
     */
    @Test
    fun testKeyCombine() {
        // 获得方法的key合并器: 兼容方法返回类型是CompletableFuture
        val keyCombiner = RpcClientMethodGuard(IGuardService::getUserByIdAsync).keyCombiner as KeyFutureSupplierCombiner<Int, User>
        /*val keyCombiner = KeyFutureSupplierCombiner<Int, User>{ id ->
            Thread.sleep(10)
            val user = User(id, randomString(7))
            CompletableFuture.completedFuture(user)
        }*/
        val futures = ArrayList<CompletableFuture<User>>()
        for (i in (0..2)) {
            futures.add(keyCombiner.add(1))
        }
        futures.print()
    }

    /**
     * 测试key合并 -- 通过调用目标方法来 同步调用
     */
    @Test
    fun testKeyCombine2() {
        // 同步调用
        makeThreads(3){
            val id = 1
            val u = service.getUserById(id)
            println("调用服务[IGuardService.getUserById($id)]结果： $u")
        }
        Thread.sleep(7000)
    }


    /**
     * 测试key合并 -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testKeyCombine3() {
        // 异步调用
        (0..2).forEach {
            val id = 1
            val f = service.getUserByIdAsync(id)
            f.thenAccept{
                println("调用服务[IGuardService.getUserById($id)]结果： $it")
            }
        }
        Thread.sleep(7000)
    }

    /************************* 测试group合并  **************************/
    /**
     * 测试group合并 -- 手动调用
     */
    @Test
    fun testGroupCombine() {
        // 获得方法的key合并器: 兼容方法返回类型是CompletableFuture
        val groupCombiner = RpcClientMethodGuard(IGuardService::getUserByNameAsync).groupCombiner as GroupFutureSupplierCombiner<String, User, User>
        /*var id = 0
        val groupCombiner = GroupFutureSupplierCombiner<String, User, User>("id"){ names ->
            val us = names.map { name ->
                User(id++, name)
            }
            CompletableFuture.completedFuture(us)
        }*/
        val futures = ArrayList<CompletableFuture<User>>()
        for (i in (0..2)) {
            futures.add(groupCombiner.add(randomString(7)))
        }
        futures.print()
    }

    /**
     * 测试group合并 -- 通过调用目标方法来 同步调用
     */
    @Test
    fun testGroupCombine2() {
        // 同步调用
        makeThreads(3){
            val name = randomString(7)
            val u = service.getUserByName(name)
            println("调用服务[IGuardService.getUserByName($name)]结果： $u")
        }
        Thread.sleep(7000)
    }

    /**
     * 测试group合并 -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testGroupCombine3() {
        // 异步调用
        for (i in (0..2)) {
            val name = randomString(7)
            val f = service.getUserByNameAsync(name)
            f.thenAccept {
                println("调用服务[IGuardService.getUserByName($name)]结果： $it")
            }
        }
        Thread.sleep(7000)
    }

    /************************* 测试后备 **************************/
    @Test
    fun testFallback(){
        val user = service.getUserWhenException(1)
        println("收到异常退回的结果: " + user)
    }

    /************************* 测试统计+断路 **************************/
    @Test
    fun testCircuitBreaker(){
        for(i in 0..10000) {
            try {
                val user = service.getUserWhenRandomException(1)
                println("成功: " + user)
            } catch (e: Throwable) {
                //println("异常: " + e)
                e.printStackTrace()
            }
            Thread.sleep(1000)
        }
    }

}