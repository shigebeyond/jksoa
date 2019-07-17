package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.StorageType
import com.indeed.lsmtree.core.StoreBuilder
import com.indeed.util.compress.CompressionCodec
import com.indeed.util.serialization.LongSerializer
import com.indeed.util.serialization.Serializer
import com.indeed.util.serialization.StringSerializer
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.getOrPutOnce
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
        protected override val autoSync: Boolean = false, // 是否自动同步到磁盘
        storageType: StorageType = StorageType.INLINE,
        codec: CompressionCodec? = null
) : LsmMqWriter() {

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


    init {
        // 创建队列存储
        val queueStoreDir = File(rootDir, "queue") // 子目录是queue
        queueStore = StoreBuilder(queueStoreDir, LongSerializer(), FstObjectSerializer() as Serializer<Message>) // key是消息id, value是消息
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()

        // 创建进度存储
        val progressStoreDir = File(rootDir, "progress") // 子目录是progress
        progressStore = StoreBuilder(progressStoreDir, StringSerializer(), LongSerializer()) // key为分组名, value是读进度对应的消息id
                .setMaxVolatileGenerationSize((8 * 1024 * 1024).toLong())
                .setStorageType(storageType)
                .setCodec(codec)
                .build()

        // 初始化最大的消息id
        val startId:Long? = queueStore.last()?.value?.id
        maxId = AtomicLong(if(startId == null) 0L else startId)
    }

}