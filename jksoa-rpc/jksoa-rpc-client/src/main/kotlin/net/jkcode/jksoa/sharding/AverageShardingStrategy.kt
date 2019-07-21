package net.jkcode.jksoa.sharding

import java.util.*

/**
 * 分片策略: 平均分
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 9:01 AM
 */
class AverageShardingStrategy : IShardingStrategy {

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每节点对应的一组分片序号(比特集)
     */
    public override fun sharding(shardingNum: Int, nodeNum: Int): Array<BitSet> {
        // 每节点对应的一组分片序号(比特集)
        val shds = Array<BitSet>(nodeNum){
            BitSet()
        }

        // 1 整除的分片部分
        val nodePerShd = shardingNum / nodeNum // 每节点的分片数, 称为一段
        // 遍历每节点来分配
        for(iNode in 0 until nodeNum){
            // 分配一段的分片序号
            for(iShd in (iNode * nodePerShd) until (iNode + 1) * nodePerShd)
                shds[iNode].set(iShd)
        }

        // 2 不能整除的分片部分
        val assigedShdNum = nodePerShd *  nodeNum // 已分配分片数
        // 遍历剩下分片来分配
        var iNode = 0
        for(iShd in assigedShdNum until shardingNum)
            shds[iNode++].set(iShd)

        return shds

    }

}