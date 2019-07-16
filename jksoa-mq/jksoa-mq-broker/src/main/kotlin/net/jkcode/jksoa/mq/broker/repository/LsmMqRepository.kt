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
import net.jkcode.jkmvc.common.getWritableFinalField
import net.jkcode.jksoa.mq.broker.common.FstObjectSerializer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.server.IRpcServer
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * 消息的仓库
 *    使用lsmtree实现的kv存储, 来保存队列+进度
 *    1. 队列存储: 子目录是queue, key是消息id, value是消息
 *    2. 进度存储: 子目录是progress, key为分组名, value是读进度对应的消息id
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
class LsmMqRepository(
        protected val topic: String, // 主题
        storageType: StorageType = StorageType.INLINE,
        codec: CompressionCodec? = null
) : IMqRepository {

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
        protected val idProp = Message::class.java.getWritableFinalField("id")

        /**
         * 最大消息id在进度存储中的键名
         */
        protected val maxIdProgressKey = "_maxId"

        /**
         * <topic, 仓库>
         */
        protected val repositories: ConcurrentHashMap<String, LsmMqRepository> = ConcurrentHashMap()

        init{
            // 加载旧的仓库
            val dir = File(dataDir)
            val topics = dir.list()
            // TODO: topic目录的判断要更严禁些, 要确定他是否真的存有队列数据
            if(topics != null)
                for(topic in topics)
                    repositories.put(topic, LsmMqRepository(topic))
        }

        /**
         * 根据topic创建仓库
         * @param topic
         * @return
         */
        public fun createRepositoryIfAbsent(topic: String): LsmMqRepository {
            return repositories.getOrPutOnce(topic){
                LsmMqRepository(topic)
            }
        }

        /**
         * 根据topic获得仓库
         * @param topic
         * @return
         */
        public fun getRepository(topic: String): LsmMqRepository {
            val result = repositories[topic]
            if(result == null) {
                val myBroker = IRpcServer.current()?.serverName
                throw MqException("Broker [$myBroker] has no queue for topic [$topic]")
            }

            return result
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
    protected lateinit var queueStore: Store<Long, Message>

    /**
     * 进度存储, 有2种情况
     *    1. 如果特定的key == "_maxId", 则value是当前队列的最大消息id
     *    2. 否则, key为分组名, value是读进度对应的消息id
     */
    protected lateinit var progressStore: Store<String, Long>

    /**
     * 最大的消息id
     */
    protected lateinit var maxId: AtomicLong

    init {
        // 创建队列存储
        val queueStoreDir = File(rootDir, "queue")
        queueStore = StoreBuilder(queueStoreDir, LongSerializer(), FstObjectSerializer() as Serializer<Message>)
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()

        // 创建进度存储
        val progressStoreDir = File(rootDir, "progress")
        progressStore = StoreBuilder(progressStoreDir, StringSerializer(), LongSerializer())
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()

        // 初始化最大的消息id
        val startId:Long? = progressStore.get(maxIdProgressKey)
        maxId = AtomicLong(if(startId == null) 0L else startId)
    }

    /**
     * 批量保存多个消息
     * @param msgs
     */
    public override fun saveMessages(msgs: List<Message>){
        for(msg in msgs) {
            // 由broker端生成消息id, 保证在同一个topic下有序
            if(msg.id == 0L)
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
     * 根据范围查询多个消息
     * @param startId 开始的id
     * @param limit
     * @param inclusive 是否包含开始的id
     * @return
     */
    public override fun getMessagesByRange(startId: Long, limit: Int, inclusive: Boolean): List<Message> {
        // 获得按key有序的迭代器
        val itr = queueStore.iterator(startId, inclusive)
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
     * 根据分组查询多个消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    public override fun getMessagesByGroup(group: String, limit: Int): List<Message>{
        val startId:Long? = progressStore.get(group)
        val result = getMessagesByRange(if(startId == null) 0L else startId, limit, false)
        if(!result.isEmpty()){
            // 保存该分组的读进度
            progressStore.put(group, result.last().id)
            // 同步进度文件
            progressStore.sync()
        }
        return result
    }

    /**
     * 查询单个消息
     * @param id
     * @return
     */
    public override fun getMessage(id: Long): Message? {
        return queueStore.get(id)
    }

    /**
     * 删除单个消息
     * @param id
     * @return
     */
    public override fun deleteMessage(id: Long) {
        queueStore.delete(id)
        queueStore.sync()
    }

}