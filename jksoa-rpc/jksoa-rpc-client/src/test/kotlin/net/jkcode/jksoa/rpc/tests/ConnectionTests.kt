package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkutil.common.randomInt
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jksoa.rpc.loadbalance.ILoadBalancer
import net.jkcode.jksoa.rpc.loadbalance.WeightCollection
import net.jkcode.jkutil.common.SimpleObjectPool
import net.jkcode.jkutil.common.makeThreads
import net.jkcode.jkutil.common.mapToArray
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-31 8:15 PM
 */
class ConnectionTests {

    // 耗时 19083 ms
    @Test
    fun testPool(){
        val counter = AtomicInteger(0)
        val pool = SimpleObjectPool(10){
            Integer(counter.incrementAndGet())
        }
        run {
            val obj = pool.borrowObject()
            //println(obj)
            pool.returnObject(obj)
        }
    }

    // 耗时 7813 ms
    @Test
    fun testArray(){
        val counter = AtomicInteger(0)
        val pool = (0 until 10).mapToArray {
            Integer(it)
        }
        run {
            val obj = pool[counter.incrementAndGet() % 10]
            //println(obj)
        }
    }

    fun run(action: ()->Unit){
        val requests = 10000000
        val threads = Executors.newFixedThreadPool(10)
        val latch = CountDownLatch(requests)
        val start = System.currentTimeMillis()
        for(i in 0..requests) {
            threads.execute {
                action()
                latch.countDown()
            }
        }
        latch.await()
        val runtime = System.currentTimeMillis() - start
        println("耗时 $runtime ms")
        threads.shutdown()
    }

}