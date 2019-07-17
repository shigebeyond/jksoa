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