package com.jksoa.job

import com.jkmvc.common.randomBoolean

/**
 * 作业分片策略: 平均分
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 9:01 AM
 */
class AverageJobShardingStrategy : IJobShardingStrategy {

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每片对应的节点序号
     */
    public override fun sharding(shardingNum: Int, nodeNum: Int): IntArray {
        val shd2Node = IntArray(shardingNum)
        // 随机倒序, 让分片更均衡
        val reversed = randomBoolean()
        // 升序: 从0到nodeNum
        // 倒序: 从nodeNum到0
        var iNode = if(reversed) nodeNum else 0
        shd2Node.mapIndexed { i, v ->
            shd2Node[i] = iNode
            iNode = if(reversed)
                        (iNode - 1 + nodeNum) % nodeNum
                    else
                        (iNode + 1) % nodeNum
        }
        return shd2Node
    }


}