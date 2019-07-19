package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.Store
import com.indeed.lsmtree.core.StoreBuilder
import com.indeed.util.serialization.LongSerializer
import com.indeed.util.serialization.Serializer
import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.broker.BrokerConfig
import net.jkcode.jksoa.mq.broker.serialize.FstObjectSerializer
import net.jkcode.jksoa.mq.broker.repository.IDelayMessageRepository
import net.jkcode.jksoa.mq.common.Message
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 延迟消息的仓库
 *    1 key是时间戳, value是延迟消息id列表
 *    2 为了保证key值(即时间戳)的唯一, 因此只能由定时触发, 不能由定量触发, 通过简单将定量阀值设为int最大值, 基本避免定量触发
 *    3 TODO:  一个消息可以延迟发送多次, 但每次延迟发送的间隔是不一样的
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 12:41 PM
 */
object LsmDelayMessageRepository : IDelayMessageRepository {

    /**
     * 延迟队列名
     *    业务开发别用这个名字来作为topic
     */
    private val delayQueueName: String = "_delayQueue"

    /**
     * 队列存储目录
     */
    private val queueDir = BrokerConfig.dataDir + File.separatorChar + delayQueueName

    /**
     * 队列存储
     *   目录是_delayQueue
     *   key是时间戳, value是延迟消息id列表
     */
    private lateinit var queueStore: Store<Long, List<MessageId>>

    /**
     * 延迟消息id合并器
     *    为了保证key值(即时间戳)的唯一, 因此只能由定时触发, 不能由定量触发, 通过简单将定量阀值设为int最大值, 基本避免定量触发
     */
    private val idCombiner = GroupRunCombiner(Int.MAX_VALUE, 100, this::saveDelayMessageIds)

    init {
        // 创建队列存储
        val queueStoreDir = File(queueDir)
        queueStore = StoreBuilder(queueStoreDir, LongSerializer(), FstObjectSerializer() as Serializer<List<MessageId>>) // key是时间戳, value是延迟消息id列表
                .setMaxVolatileGenerationSize(BrokerConfig.maxVolatileGenerationSize)
                .setStorageType(BrokerConfig.storageType)
                .setCodec(BrokerConfig.compressionCodec)
                .build()
    }

    /**
     * 添加延迟的消息id
     * @param topic
     * @param id
     * @return
     */
    public override fun addDelayMessageId(topic: String, id: Long): CompletableFuture<Unit> {
        return idCombiner.add(topic to id)
    }

    /**
     * 批量保存延迟的消息id
     * @param ids
     */
    private fun saveDelayMessageIds(ids: List<MessageId>) {
        val now = System.currentTimeMillis()
        val delay = now + BrokerConfig.mqDelaySeconds * 1000 // 延迟固定秒
        queueStore.put(delay, ids)
        queueStore.sync()
    }

    /**
     * 取出到期的延迟消息
     * @return
     */
    public override fun pollExpiredDelayMessages(): List<Message> {
        val keys = ArrayList<Long>()
        val msgs = ArrayList<Message>()
        val now = System.currentTimeMillis()
        val itr = queueStore.iterator(now, true)
        for(entry in itr){
            // 收集key
            keys.add(entry.key)

            // 收集消息
            val mids = entry.value // 延迟消息id列表
            for((topic, id) in mids){
                // 根据topic获得仓库
                val repository = LsmMessageRepository.getRepository(topic)
                // 查询消息
                val msg = repository.getMessage(id)
                if(msg != null)
                    msgs.add(msg)
            }
        }

        // 删除出队的延迟消息
        for(key in keys)
            queueStore.delete(key)
        queueStore.sync()

        return msgs
    }
}