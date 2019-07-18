package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.StoreBuilder
import com.indeed.util.serialization.LongSerializer
import com.indeed.util.serialization.Serializer
import com.indeed.util.serialization.StringSerializer
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jksoa.mq.broker.BrokerConfig
import net.jkcode.jksoa.mq.broker.common.FstObjectSerializer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.mq.registry.TopicRegex
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
class LsmMessageRepository(
        protected val topic: String, // 主题
        protected override val autoSync: Boolean = BrokerConfig.autoSync // 是否自动同步到磁盘
) : LsmMessageWriter() {

    companion object{

        /**
         * <topic, 仓库>
         */
        protected val repositories: ConcurrentHashMap<String, LsmMessageRepository> = ConcurrentHashMap()

        init{
            // 加载旧的仓库
            val dir = File(BrokerConfig.dataDir)
            val topics = dir.list()
            if(topics != null)
                for(topic in topics)
                    if(isTopicStoreDirectory(File(dir, topic)))
                        repositories.put(topic, LsmMessageRepository(topic))
        }

        /**
         * 检查是否是主题存储的目录
         * @param dir
         * @return
         */
        private fun isTopicStoreDirectory(dir: File): Boolean {
            return TopicRegex.matches(dir.name) // 主题名
                    && dir.isDirectory
                    && File(dir, "queue").isDirectory // 队列存储子目录
                    && File(dir, "progress").isDirectory // 进度存储子目录
        }

        /**
         * 根据topic创建仓库
         * @param topic
         * @return
         */
        public fun createRepositoryIfAbsent(topic: String): LsmMessageRepository {
            return repositories.getOrPutOnce(topic){
                LsmMessageRepository(topic)
            }
        }

        /**
         * 根据topic获得仓库
         * @param topic
         * @return
         */
        public fun getRepository(topic: String): LsmMessageRepository {
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
    protected val rootDir = BrokerConfig.dataDir + File.separatorChar + topic

    /**
     * 队列存储子目录: queue
     */
    protected val queueDir = rootDir + File.separatorChar + "queue"

    /**
     * 进度存储子目录: progress
     */
    protected val progressDir = rootDir + File.separatorChar + "progress"

    init {
        // 创建队列存储
        val queueStoreDir = File(queueDir) // 子目录是queue
        queueStore = StoreBuilder(queueStoreDir, LongSerializer(), FstObjectSerializer() as Serializer<Message>) // key是消息id, value是消息
                .setMaxVolatileGenerationSize(BrokerConfig.maxVolatileGenerationSize)
                .setStorageType(BrokerConfig.storageType)
                .setCodec(BrokerConfig.compressionCodec)
                .build()

        // 创建进度存储
        val progressStoreDir = File(progressDir) // 子目录是progress
        progressStore = StoreBuilder(progressStoreDir, StringSerializer(), LongSerializer()) // key为分组名, value是读进度对应的消息id
                .setMaxVolatileGenerationSize(BrokerConfig.maxVolatileGenerationSize)
                .setStorageType(BrokerConfig.storageType)
                .setCodec(BrokerConfig.compressionCodec)
                .build()

        // 初始化最大的消息id
        val startId:Long? = queueStore.last()?.value?.id
        maxId = AtomicLong(if(startId == null) 0L else startId)
    }

}