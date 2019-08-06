package net.jkcode.jksoa.mq.broker

import com.indeed.lsmtree.core.StorageType
import com.indeed.util.compress.CompressionCodec
import com.indeed.util.compress.GzipCodec
import com.indeed.util.compress.SnappyCodec
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.fileSize2Bytes

/**
 * broker配置
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 2:15 PM
 */
object BrokerConfig {

    /**
     * 中转者配置
     */
    private val config = Config.instance("broker", "yaml")

    /**
     * 消息延迟发送的秒数
     */
    public val mqDelaySeconds: Long = config["mqDelaySeconds"]!!

    /**
     * 触发批量同步的定时时间
     */
    public val batchSyncTimeoutMillis: Long = config["batchSyncTimeoutMillis"]!!

    /**
     * 触发批量同步的写操作次数
     */
    public val batchSyncQuota: Int = config["batchSyncQuota"]!!

    /**
     * 是否批量同步
     *    只有定量与定时都大于0才批量同步, 否则立即同步
     */
    public val batchSync: Boolean = batchSyncQuota > 0 && batchSyncTimeoutMillis > 0

    /**
     * 数据存储目录
     */
    public val dataDir: String = config["dataDir"]!!

    /**
     * 易变代存储的最大大小，单位 B K M G T
     */
    public val maxVolatileGenerationSize: Long = fileSize2Bytes(config["maxVolatileGenerationSize"]!!)

    /**
     * 存储类型  1. inline 不压缩 2. block_compressed　压缩
     */
    public val storageType: StorageType =
            if("block_compressed".equals(config["storageType"]!!))
                StorageType.BLOCK_COMPRESSED
            else
                StorageType.INLINE

    /**
     * 压缩类型　1. gzip 2. snappy
     */
    public val compressionCodec: CompressionCodec? =
            if("gzip".equals(config["compressionCodec"]!!))
                GzipCodec()
            else if("snappy".equals(config["compressionCodec"]!!))
                SnappyCodec()
            else
                null

}