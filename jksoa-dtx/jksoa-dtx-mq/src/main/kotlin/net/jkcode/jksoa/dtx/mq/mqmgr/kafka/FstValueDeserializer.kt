package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jkutil.serialize.FstSerializer
import net.jkcode.jkutil.serialize.ISerializer
import org.apache.kafka.common.serialization.Deserializer

/**
 * 使用fst将字节数组反序列化为对象
 */
class FstValueDeserializer : Deserializer<Any> {

    protected val fst: FstSerializer = ISerializer.instance("fst") as FstSerializer

    override fun deserialize(topic: String, data: ByteArray): Any? {
        return fst.unserialize(data)
    }
}