package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jkmvc.common.print
import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.client.combiner.GroupRpcRequestCombiner
import net.jkcode.jksoa.client.combiner.KeyRpcRequestCombiner
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.example.IExampleService
import net.jkcode.jksoa.example.User
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
     * 测试 KeyRpcRequestCombiner -- 手动调用
     */
    @Test
    fun testKeyRpcRequestCombiner() {
        val keyCombiner = KeyRpcRequestCombiner(IExampleService::getUserById)
        val futures = ArrayList<CompletableFuture<User>>()
        for (i in (0..2)) {
            futures.add(keyCombiner.add(1))
        }
        futures.print()
    }

    /**
     * 测试 GroupRpcRequestCombiner -- 通过调用目标方法来 同步调用
     */
    @Test
    fun testKeyRpcRequestCombiner2() {
        // 同步调用
        val run = {
            val id = 1
            val u = combinerService.getUserById(id)
            println("调用服务[IExampleService.getUserById($id)]结果： $u")
        }
        makeThreads(3, run)
    }


    /**
     * 测试 testKeyRpcRequestCombiner -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testKeyRpcRequestCombiner3() {
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
     * 测试 GroupRpcRequestCombiner -- 手动调用
     */
    @Test
    fun testGroupRpcRequestCombiner() {
        val groupCombiner = GroupRpcRequestCombiner<String, User, User>(IExampleService::listUsersByName, "name", "", true, 100, 100)
        val futures = ArrayList<CompletableFuture<User>>()
        for (i in (0..2)) {
            futures.add(groupCombiner.add(randomString(7))!!)
        }
        futures.print()
    }

    /**
     * 测试 GroupRpcRequestCombiner -- 通过调用目标方法来 同步调用
     */
    @Test
    fun testGroupRpcRequestCombiner2() {
        // 同步调用
        val run = {
            val name = randomString(7)
            val u = combinerService.getUserByName(name)
            println("调用服务[IExampleService.getUserByName($name)]结果： $u")
        }
        makeThreads(3, run)
    }

    /**
     * 测试 GroupRpcRequestCombiner -- 通过调用目标方法来 异步调用
     */
    @Test
    fun testGroupRpcRequestCombiner3() {
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