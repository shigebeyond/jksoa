package net.jkcode.jksoa.mq.broker.repository.lsm

import net.jkcode.jkmvc.common.UnitFuture
import net.jkcode.jkmvc.common.getWritableFinalField
import net.jkcode.jkmvc.flusher.CounterFlusher
import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

/**
 * 消息的仓库 -- 写处理
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 10:01 AM
 */
abstract class LsmMqWriter : LsmMqReader() {

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
    protected var autoSync: Boolean = false

    /**
     * 定量刷盘, 提升刷盘效率
     */
    protected val syncCounter: CounterFlusher? = if(autoSync) null else object: CounterFlusher(100, 100) {
        // 处理刷盘
        override fun handleFlush(): Boolean {
            // 同步到磁盘
            queueStore.sync()
            return true
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
    protected fun putMessage(msg: Message) {
        // 由broker端生成消息id, 保证在同一个topic下有序
        if (msg.id == 0L)
            idProp.set(msg, maxId.incrementAndGet())
        // 保存消息
        queueStore.put(msg.id, msg)
    }

    /**
     * 保存单个消息
     * @param msg
     */
    public override fun saveMessage(msg: Message){
        putMessage(msg)

        trySync(1)
    }

    /**
     * 批量保存多个消息
     * @param msgs
     */
    public override fun batchSaveMessages(msgs: List<Message>){
        for(msg in msgs)
            putMessage(msg)

        trySync(msgs.size)
    }

    /**
     * 删除单个消息
     * @param id
     * @return
     */
    public override fun deleteMessage(id: Long) {
        queueStore.delete(id)

        trySync(1)
    }

    /**
     * 批量删除多个消息
     * @param id
     */
    public override fun batchDeleteMessages(ids: List<Long>){
        for(id in ids)
            queueStore.delete(id)

        trySync(ids.size)
    }
}