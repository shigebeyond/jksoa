package net.jkcode.jksoa.mq.broker.serialize

import com.indeed.util.serialization.LengthVIntSerializer
import com.indeed.util.serialization.Serializer
import org.nustaq.serialization.FSTConfiguration
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * 使用 fst 来序列化对象
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-14 2:25 PM
 */
class FstObjectSerializer : Serializer<Any> {

    companion object {

        /**
         * 大小序列器
         */
        public val lengthSerializer = LengthVIntSerializer()

        /**
         * fst配置
         */
        public val conf = FSTConfiguration.createDefaultConfiguration()
    }

    /**
     * 写对象
     */
    @Throws(IOException::class)
    public override fun write(obj: Any, out: DataOutput) {
        // 转字节
        val bytes = conf.asByteArray(obj)
        // 写大小
        lengthSerializer.write(bytes.size, out)
        // 写字节
        out.write(bytes)
    }

    /**
     * 读对象
     */
    @Throws(IOException::class)
    public override fun read(`in`: DataInput): Any {
        // 读大小
        val length = lengthSerializer.read(`in`)!!
        // 读字节
        val bytes = ByteArray(length)
        `in`.readFully(bytes)
        // 转对象
        return conf.getObjectInput(bytes).readObject()
    }

}