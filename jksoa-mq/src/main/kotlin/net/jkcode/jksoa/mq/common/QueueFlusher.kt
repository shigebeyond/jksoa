package net.jkcode.jksoa.mq.common

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkmvc.common.drainTo
import net.jkcode.jksoa.common.CommonThreadPool
import net.jkcode.jksoa.common.CommonMilliTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jksoa.common.IRpcRequest
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * 请求 + 上下文
 */
public typealias RequestContext = Pair<IRpcRequest, ChannelHandlerContext>

/**
 * 队列刷盘器
 *    定时刷盘 + 定量刷盘
 *    注意: 使用 ConcurrentLinkedQueue 来做队列, 其 size() 是遍历性能慢, 尽量使用 isEmpty()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-12 5:52 PM
 */
abstract class QueueFlusher<E> (protected val flushTimeoutMillis: Long /* 触发刷盘的定时时间 */,
                                protected val flushSize: Int /* 触发刷盘的队列大小 */
) {
    /**
     * 队列
     */
    private val queue: ConcurrentLinkedQueue<E> = ConcurrentLinkedQueue()

    /**
     * 全局共享的可复用的用于存储临时元素的 List 对象
     */
    private val tmpItems:ThreadLocal<ArrayList<E>> = ThreadLocal.withInitial {
        ArrayList<E>()
    }

    /**
     * 刷盘的定时任务
     */
    private var flushTimeout: Timeout = CommonMilliTimer.newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            flush()
        }
    }, flushTimeoutMillis, TimeUnit.MILLISECONDS)

    /**
     * 单个元素入队
     * @param e
     * @return
     */
    public fun add(e: E): Boolean {
        val result = queue.offer(e)
        if(queue.size >= flushSize) // 定量刷盘
            flush()
        return result
    }

    /**
     * 多个元素入队
     * @param c
     * @return
     */
    public fun addAll(c: Collection<out E>): Boolean {
        val result = queue.addAll(c)
        if(queue.size >= flushSize) // 定量刷盘
            flush()
        return result
    }

    /**
     * 将队列中的元素刷掉
     */
    protected fun flush(){
        CommonThreadPool.execute{
            val items = tmpItems.get()
            try {
                while (queue.isNotEmpty()) {
                    // 取出元素
                    queue.drainTo(items, flushSize)

                    // 处理刷盘
                    handleFlush(items)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                items.clear()
            }
        }
    }

    /**
     * 处理刷盘的元素
     * @param items
     */
    protected abstract fun handleFlush(items: List<E>)

}