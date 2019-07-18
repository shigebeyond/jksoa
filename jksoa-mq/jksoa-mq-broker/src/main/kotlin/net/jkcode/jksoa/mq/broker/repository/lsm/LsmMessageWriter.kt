package net.jkcode.jksoa.mq.broker.repository.lsm

import net.jkcode.jkmvc.common.UnitFuture
import net.jkcode.jkmvc.common.getWritableFinalField
import net.jkcode.jkmvc.common.mapToArray
import net.jkcode.jkmvc.flusher.CounterFlusher
import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

/**
 * 消息的仓库 -- 写处理
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 10:01 AM
 */
abstract class LsmMessageWriter : LsmMessageReader() {

    companion object{
        /**
         * 消息的id属性
         */
        protected val idProp = Message::class.java.getWritableFinalField("id")

    }

    /**
     * 最大的消息id
     */
    protected lateinit var maxId: AtomicLong

    /**
     * 是否自动同步到磁盘
     */
    protected abstract val autoSync: Boolean

    /**
     * 定量刷盘, 提升刷盘效率
     */
    protected val syncCounter: CounterFlusher? by lazy{
        if(autoSync)
            null
        else
            object: CounterFlusher(100, 100) {
                // 处理刷盘
                override fun handleFlush(reqCount: Long): Boolean {
                    // print(if(reqCount < flushSize) "定时" else "定量")
                    // println("sync, 操作计数 from [$reqCount] to [${requestCount()}] ")
                    // 同步到磁盘
                    queueStore.sync()
                    return true
                }
            }
    }

    /**
     * 尝试同步
     * @param num
     * @return
     */
    protected fun trySync(num: Int): CompletableFuture<Unit> {
        // 立即同步
        if(autoSync) {
            queueStore.sync()
            return UnitFuture
        }

        // 延迟同步
        return syncCounter!!.add(num)
    }

    /**
     * 添加消息, 会生成消息id
     * @param msg
     */
    protected fun doPutMessage(msg: Message): Long {
        // 由broker端生成消息id, 保证在同一个topic下有序
        if (msg.id == 0L)
            idProp.set(msg, maxId.incrementAndGet())

        // 保存消息
        queueStore.put(msg.id, msg)
        return msg.id
    }

    /**
     * 保存单个消息
     * @param msg 消息
     * @return 消息id
     */
    public override fun putMessage(msg: Message): CompletableFuture<Long> {
        val id = doPutMessage(msg)

        return trySync(1).thenApply { id }
    }

    /**
     * 批量保存多个消息
     * @param msgs 消息
     * @return 消息id
     */
    public override fun batchPutMessages(msgs: List<Message>): CompletableFuture<Array<Long>> {
        val ids = msgs.mapToArray { msg ->
            doPutMessage(msg)
        }

        return trySync(msgs.size).thenApply { ids }
    }

    /**
     * 删除单个消息
     * @param id
     * @return
     */
    public override fun deleteMessage(id: Long): CompletableFuture<Unit> {
        queueStore.delete(id)

        return trySync(1)
    }

    /**
     * 批量删除多个消息
     * @param id
     * @return
     */
    public override fun batchDeleteMessages(ids: List<Long>): CompletableFuture<Unit> {
        for(id in ids)
            queueStore.delete(id)

        return trySync(ids.size)
    }
}