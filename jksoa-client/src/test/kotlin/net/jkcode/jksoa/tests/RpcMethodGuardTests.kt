package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jkmvc.common.print
import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.client.referer.RpcMethodGuard
import net.jkcode.jksoa.example.IExampleService
import net.jkcode.jksoa.example.User
import net.jkcode.jksoa.guard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jksoa.guard.combiner.KeyFutureSupplierCombiner
import org.junit.Test
import java.util.concurrent.CompletableFuture

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RpcRequestCombinerTests {

    val combinerService = Referer.getRefer<IExampleService>()

    /************************* 测试 KeyRpcRequestCombiner **************************/
    /**
     * 测试ke合并 -- 手动调用
     */
    @Test
    fun testKeyCombine() {
        // 获得方法的key合并器: 兼容方法返回类型是CompletableFuture
        val keyCombiner = RpcMethodGuard.instance(IExampleService::getUserByIdAsync).keyCombiner as KeyFutureSupplierCombiner<Int, User>
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
        val run = {
            val id = 1
            val u = combinerService.getUserById(id)
            println("调用服务[IExampleService.getUserById($id)]结果： $u")
        }
        makeThreads(3, run)
    }


    /**
     * 测试key合并 -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testKeyCombine3() {
        // 异步调用
        (0..2).forEach {
            val id = 1
            val f = combinerService.getUserByIdAsync(id)
            f.thenAccept{
                println("调用服务[IExampleService.getUserById($id)]结果： $it")
            }
        }
        Thread.sleep(2000)
    }

    /************************* 测试 GroupRpcRequestCombiner **************************/
    /**
     * 测试group合并 -- 手动调用
     */
    @Test
    fun testGroupCombine() {
        // 获得方法的key合并器: 兼容方法返回类型是CompletableFuture
        val groupCombiner = RpcMethodGuard.instance(IExampleService::getUserByNameAsync).groupCombiner as GroupFutureSupplierCombiner<String, User, User>
        val futures = ArrayList<CompletableFuture<User>>()
        for (i in (0..2)) {
            futures.add(groupCombiner.add(randomString(7))!!)
        }
        futures.print()
    }

    /**
     * 测试group合并 -- 通过调用目标方法来 同步调用
     */
    @Test
    fun testGroupCombine2() {
        // 同步调用
        val run = {
            val name = randomString(7)
            val u = combinerService.getUserByName(name)
            println("调用服务[IExampleService.getUserByName($name)]结果： $u")
        }
        makeThreads(3, run)
    }

    /**
     * 测试group合并 -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testGroupCombine3() {
        // 异步调用
        for (i in (0..2)) {
            val name = randomString(7)
            val f = combinerService.getUserByNameAsync(name)
            f.thenAccept {
                println("调用服务[IExampleService.getUserByName($name)]结果： $it")
            }
        }
        Thread.sleep(2000)
    }



}