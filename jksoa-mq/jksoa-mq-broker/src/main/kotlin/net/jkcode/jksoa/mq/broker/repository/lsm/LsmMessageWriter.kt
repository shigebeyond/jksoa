package net.jkcode.jksoa.mq.broker.repository.lsm

import net.jkcode.jkmvc.common.UnitFuture
import net.jkcode.jkmvc.common.VoidFuture
import net.jkcode.jkmvc.common.getWritableFinalField
import net.jkcode.jkmvc.common.mapToArray
import net.jkcode.jkmvc.flusher.CounterFlusher
import net.jkcode.jksoa.mq.common.GroupSequence
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqBrokerLogger
import java.util.*
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
     * 是否立即同步到磁盘
     */
    protected abstract val immediateSync: Boolean

    /**
     * 定量刷盘, 提升刷盘效率
     */
    protected val syncCounter: CounterFlusher? by lazy{
        if(immediateSync)
            null
        else
            object: CounterFlusher(100, 100) {
                // 处理刷盘
                override fun handleRequests(reqCount: Int): CompletableFuture<Void> {
                    // print(if(reqCount < flushSize) "定时" else "定量")
                    // println("sync, 操作计数 from [$reqCount] to [${requestCount()}] ")
                    // 同步到磁盘
                    mqBrokerLogger.debug("LsmMessageWriter[$topic]批量同步消息到磁盘")
                    queueStore.sync()
                    indexStore.sync()
                    return VoidFuture
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
        if(immediateSync) {
            queueStore.sync()
            indexStore.sync()
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
        indexStore.put(msg.id, msg.groupIds)
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
     * 删除分组相关的消息
     *    完成后, 该消息即与该分组解除(待消费)关系
     *    所有分组都完成时, 删除该消息
     *
     * @param id
     * @param groupId
     * @return false 表示消息与分组不相关
     */
    protected fun doFinishMessage(id: Long, groupId: Int): Boolean {
        // 先查索引
        val groupIds: BitSet? = indexStore.get(id)
        if (groupIds == null)
            return false

        // 设置分组id比特为已消费
        groupIds.clear(groupId)

        // 该消息的分组已全部消费完: 删除
        if (groupIds.cardinality() == 0) {
            mqBrokerLogger.debug("消息[{}]的分组已全部消费完: 删除", id)
            queueStore.delete(id)
            indexStore.delete(id)
        } else { // 回写索引
            indexStore.put(id, groupIds)
        }
        return true
    }

    /**
     * 完成与分组相关的单个消息
     *    完成后, 该消息即与该分组解除(待消费)关系
     *    所有分组都完成时, 删除该消息
     *
     * @param id
     * @param group
     * @return
     */
    public override fun finishMessage(id: Long, group: String): CompletableFuture<Unit> {
        val groupId = GroupSequence.get(group)
        if (!doFinishMessage(id, groupId))
            return UnitFuture

        return trySync(1)
    }

    /**
     * 完成与分组相关的多个消息
     *    完成后, 该消息即与该分组解除(待消费)关系
     *    所有分组都完成时, 删除该消息
     *
     * @param ids
     * @param group
     * @return
     */
    public override fun batchFinishMessages(ids: List<Long>, group: String): CompletableFuture<Unit> {
        val groupId = GroupSequence.get(group)
        val num = ids.count { id ->
            doFinishMessage(id, groupId)
        }

        return trySync(num)
    }
}