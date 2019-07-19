package net.jkcode.jksoa.mq.broker.serialize

import com.indeed.util.serialization.Serializer
import com.indeed.util.serialization.array.LongArraySerializer

import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * 比特集序列器
 *   代理 LongArraySerializer
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-19 8:12 PM
 */
class BitSetSerializer: Serializer<BitSet> {

    companion object{

        /**
         * long数组序列器
         */
        public val longArraySerializer = LongArraySerializer()
    }

    public override fun read(`in`: DataInput): BitSet {
        val ls = longArraySerializer.read(`in`)
        return BitSet.valueOf(ls)
    }

    public override fun write(bs: BitSet, out: DataOutput) {
        longArraySerializer.write(bs.toLongArray(), out)
    }


}