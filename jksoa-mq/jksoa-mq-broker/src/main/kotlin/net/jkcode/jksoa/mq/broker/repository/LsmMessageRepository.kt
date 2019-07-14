package net.jkcode.jksoa.mq.broker.repository

import com.indeed.lsmtree.core.StorageType
import com.indeed.lsmtree.core.Store
import com.indeed.lsmtree.core.StoreBuilder
import com.indeed.util.compress.CompressionCodec
import com.indeed.util.serialization.LongSerializer
import com.indeed.util.serialization.Serializer
import com.indeed.util.serialization.StringSerializer
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jkmvc.common.getProperty
import net.jkcode.jksoa.mq.broker.common.FstObjectSerializer
import net.jkcode.jksoa.mq.common.Message
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KMutableProperty1

/**
 * 消息的仓库
 *    使用lsmtree实现的kv存储, 来保存队列+进度
 *    1. 队列存储: 子目录是queue, key是消息id, value是消息
 *    2. 进度存储: 子目录是progress, key为分组名, value是读进度对应的消息id
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
class LsmMessageRepository(
        protected val topic: String, // 主题
        storageType: StorageType = StorageType.INLINE,
        codec: CompressionCodec? = null
) : IMessageRepository {

    companion object{
        /**
         * 中转者者配置
         */
        public val config = Config.instance("broker", "yaml")

        /**
         * 数据存储目录
         */
        public val dataDir: String = config["dataDir"]!!

        /**
         * 消息的id属性
         */
        protected val idProp = Message::class.getProperty("id") as KMutableProperty1<Message, Any?>

        /**
         * 最大消息id在进度存储中的键名
         */
        protected val maxIdProgressKey = "_maxId"

        /**
         * <topic, 仓库>
         */
        protected val repositories: ConcurrentHashMap<String, LsmMessageRepository> = ConcurrentHashMap()

        /**
         * 根据topic获得仓库
         */
        public fun getOrCreateRepository(topic: String): LsmMessageRepository {
            return repositories.getOrPutOnce(topic) {
                LsmMessageRepository(topic)
            }
        }
    }

    /**
     * 该topic下的根目录
     */
    protected val rootDir = dataDir + File.separatorChar + topic

    /**
     * 队列存储
     *   key是消息id, value是消息
     */
    protected val queueStore: Store<Long, Message> by lazy{
        // 创建存储
        val storeDir = File(rootDir, "queue")
        StoreBuilder(storeDir, LongSerializer(), FstObjectSerializer() as Serializer<Message>)
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()
    }

    /**
     * 进度存储, 有2种情况
     *    1. 如果特定的key == "_maxId", 则value是当前队列的最大消息id
     *    2. 否则, key为分组名, value是读进度对应的消息id
     */
    protected val progressStore: Store<String, Long> by lazy{
        // 创建存储
        val storeDir = File(rootDir, "progress")
        StoreBuilder(storeDir, StringSerializer(), LongSerializer())
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()
    }

    /**
     * 最大的消息id
     */
    protected val maxId: AtomicLong by lazy{
        val startId:Long? = progressStore.get(maxIdProgressKey)
        AtomicLong(if(startId == null) 0L else startId)
    }

    /**
     * 批量保存消息
     * @param msgs
     */
    public override fun saveMessages(msgs: List<Message>){
        for(msg in msgs) {
            // 由broker端生成消息id, 保证在同一个topic下有序
            idProp.set(msg, maxId.incrementAndGet())
            // 保存消息
            queueStore.put(msg.id, msg)
        }

        // 保存最大消息id
        progressStore.put(maxIdProgressKey, maxId.get())

        // 同步文件
        queueStore.sync()
        progressStore.sync()
    }

    /**
     * 根据范围查询消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    public override fun getMessagesByRange(startId: Long, limit: Int): List<Message> {
        // 获得按key有序的迭代器
        val itr = queueStore.iterator(startId, true)
        var i = 0
        val result = ArrayList<Message>()
        while (itr.hasNext() && i < limit) {
            val entry = itr.next()
            result.add(entry.value)
            i++
        }
        return result
    }

    /**
     * 根据分组查询消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    public override fun getMessagesByGroup(group: String, limit: Int): List<Message>{
        val startId:Long? = progressStore.get(group)
        val result = getMessagesByRange(if(startId == null) 0L else startId, limit)
        if(!result.isEmpty()){
            // 保存该分组的读进度
            progressStore.put(group, result.last().id)
            // 同步进度文件
            progressStore.sync()
        }
        return result
    }

    /**
     * 删除消息
     * @param id
     * @return
     */
    public override fun deleteMsg(id: Long) {
        queueStore.delete(id)
    }

}