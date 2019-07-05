package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import org.junit.Test
import java.util.*
import java.util.concurrent.CompletableFuture

class RequestQueueFlusherTest{

    /**
     * 请求队列
     */
    protected val queue: RequestQueueFlusher<Int, Void> = object: RequestQueueFlusher<Int, Void>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(ids: List<Int>, reqs: ArrayList<Pair<Int, CompletableFuture<Void>>>): Boolean {
            println("批量处理请求: $ids")
            return true
        }
    }

    @Test
    fun testAdd(){
        val futures = LinkedList<CompletableFuture<*>>()
        makeThreads(10){i ->
            val future = queue.add(i)
            futures.add(future)
        }
        CompletableFuture.allOf(*futures.toTypedArray()).get()
        println("over")
    }
}