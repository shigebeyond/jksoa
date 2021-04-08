package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import io.netty.channel.DefaultEventLoop
import net.jkcode.jkutil.scope.ClosingOnShutdown
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.errors.WakeupException
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * 可执行的消费者
 *    由于KafkaConsumer不是线程安全的, 因此每个KafkaConsumer绑定固定一个线程
 *    同时为了减少线程, 支持同一个KafkaConsumer多次调用subscribe()来订阅多个主题, subscribe()需在绑定的线程中执行, 否则报错: KafkaConsumer is not safe for multi-threaded access
 *    详见executeAndRestartPoll() 先中断poll死循环, 然后执行任务, 最后重启poll死循环
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
class ExecutableConsumer<K, V>(
        protected val delegate: Consumer<K, V>, // 代理的消费者
        protected val listeners: MutableMap<String, (V)->Unit> // 消费处理: <主题, 监听器>
) : Consumer<K, V> by delegate {

    /**
     * 绑定单线程
     */
    protected val singleThread = DefaultEventLoop()

    /**
     * 是否正在运行, 用于中断死循环
     */
    @Volatile
    protected var running = false

    init {
        // 关机时取消订阅+关闭
        ClosingOnShutdown.addClosing(object: Closeable {
            override fun close() {
                // 取消订阅
                try {
                    delegate.unsubscribe()
                } catch (ex: WakeupException) {
                }

                // 关闭
                delegate.close()
            }
        })

    }

    /**
     * 启动拉取消息的线程池
     */
    fun startPoll(){
        singleThread.submit { // 每个消费者由一个线程来拉取
            doPoll()
        }
    }

    /**
     * 真正的消费者拉取
     */
    protected fun doPoll(){
        // 标记运行中
        running = true

        // 死循环拉消息
        while (running) {
            val records = delegate.poll(1000)
            for (record in records){
                //println("revice: key =" + record.key() + " value =" + record.value() + " topic =" + record.topic())
                // 调用监听器来处理消息
                listeners[record.topic()]?.invoke(record.value())
            }
        }
    }

    /**
     * 在绑定的线程执行任务
     *   先中断poll死循环, 然后执行任务, 最后重启poll死循环
     */
    protected fun executeAndRestartPoll(task: ()->Unit){
        singleThread.execute{
            // 中断poll死循环, 让线程重新执行队列中的任务
            running = false
            // 真正的任务处理
            task()
            // 重新拉取
            doPoll()
        }
    }

    override fun unsubscribe() {
        executeAndRestartPoll {
            delegate.unsubscribe()
        }
    }

    override fun close() {
        executeAndRestartPoll {
            delegate.close()
        }
    }

    override fun close(timeout: Long, unit: TimeUnit?) {
        executeAndRestartPoll {
            delegate.close(timeout, unit)
        }
    }

    override fun close(timeout: Duration?) {
        executeAndRestartPoll {
            delegate.close(timeout)
        }
    }

    override fun subscribe(topics: Collection<String>) {
        executeAndRestartPoll {
            delegate.subscribe(topics)
        }
    }

    override fun subscribe(topics: Collection<String>, callback: ConsumerRebalanceListener) {
        executeAndRestartPoll {
            delegate.subscribe(topics, callback)
        }
    }

    override fun subscribe(pattern: Pattern, callback: ConsumerRebalanceListener) {
        executeAndRestartPoll {
            delegate.subscribe(pattern, callback)
        }
    }

    override fun subscribe(pattern: Pattern) {
        executeAndRestartPoll {
            delegate.subscribe(pattern)
        }
    }

}