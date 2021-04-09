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
 * 可执行的消费者: 带poll线程
 *    1 消费者:线程=1:1
 *    由于KafkaConsumer不是线程安全的, 因此每个KafkaConsumer绑定固定一个线程
 *    2 支持多主题
 *    同时为了减少线程, 支持同一个KafkaConsumer多次调用subscribe()来订阅多个主题
 *    3 线程安全问题
 *    subscribe()需在绑定的线程中执行, 否则报错: KafkaConsumer is not safe for multi-threaded access
 *    详见executeAndRestartPoll() 先中断poll死循环, 然后执行任务, 最后重启poll死循环(doPoll())
 *    注意: 内部线程DefaultEventLoop中的队列任务最终都会调用doPoll(), 当然不仅仅是调用doPoll()
 *    TODO: 并发下会有问题: 因为 executeAndRestartPoll() 是在外部线程中设置 `running = false`, 而 doPoll() 是在内部线程设置`running = true`, 并根据 running 来死循环
 *          而多线程下如果同时发生2次executeAndRestartPoll()执行任务, 前者刚在外部线程中设置 `running = false`, 后者紧接着进入内部线程doPoll()并设置`running = true`成功, 这样后者会进入死循环, 而导致前者的任务得不到执行
 *          优化(未完全解决): doPoll()执行前先检查DefaultEventLoop中的队列是否为空
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
class ExecutableConsumer<K, V>(
        protected val delegate: Consumer<K, V> // 代理的消费者
) : Consumer<K, V> by delegate {

    /**
     * 消费者容器
     */
    internal lateinit var container: ConcurrentExecutableConsumerContainer<K, V>

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
        // 防止重复运行
        if(running)
            return

        // 每个消费者由一个线程来拉取
        singleThread.submit {
            doPoll()
        }
    }

    /**
     * 真正的消费者拉取
     *   死循环拉取消息
     *   根据 running 来确定是否中断死循环, 给其他任务执行的机会
     */
    protected fun doPoll(){
        // 防止重复运行
        if(running)
            return

        // 标记运行中
        running = true

        // 死循环拉消息
        while (running) {
            val records = delegate.poll(1000)
            for (record in records){
                //println("revice: key =" + record.key() + " value =" + record.value() + " topic =" + record.topic())
                // 调用监听器来处理消息
                val listener = container.getListener(record.topic())
                listener?.invoke(record.value())
            }
        }
    }

    /**
     * 在绑定的线程执行任务
     *   先中断poll死循环, 然后执行任务, 最后重启poll死循环
     */
    protected fun executeAndRestartPoll(task: ()->Unit){
        // 1 外部线程处理: 中断poll死循环, 让内部线程重新执行队列中的任务
        running = false

        singleThread.execute{
            // 2 内部线程处理
            // 真正的任务处理
            task()
            // 重新拉取: 先检查内部线程的队列是否为空, 然后才拉取
            if(singleThread.pendingTasks() == 0)
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