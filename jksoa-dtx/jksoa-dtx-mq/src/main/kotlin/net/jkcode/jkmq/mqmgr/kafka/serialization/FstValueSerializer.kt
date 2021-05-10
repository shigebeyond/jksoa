package net.jkcode.jkmq.mqmgr.kafka.serialization

import net.jkcode.jkutil.serialize.FstSerializer
import net.jkcode.jkutil.serialize.ISerializer
import org.apache.kafka.common.serialization.Serializer

/**
 * 使用fst将对象序列化为字节数组
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
class FstValueSerializer : Serializer<Any> {

    protected val fst: FstSerializer = ISerializer.instance("fst") as FstSerializer

    override fun serialize(topic: String, data: Any): ByteArray? {
        return fst.serialize(data)
    }
}