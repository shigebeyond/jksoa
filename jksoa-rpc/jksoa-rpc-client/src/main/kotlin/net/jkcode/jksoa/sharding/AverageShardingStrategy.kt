package net.jkcode.jksoa.sharding

import net.jkcode.jkmvc.common.randomBoolean
import net.jkcode.jkmvc.common.randomInt

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
     * @return 每节点对应的分片序号的比特集
     */
    public override fun sharding(shardingNum: Int, nodeNum: Int): List<BitSet> {
        val node2shd = Array<BitSet>(nodeNum)

        // 1 整除的分片部分
        val nodePerShd = shardingNum / nodeNum // 每节点的分片数, 称为一段
        // 遍历每节点来分配
        for(iNode in 0 util nodeNum){
            val bs = BitSet()
            // 分配一段的分片序号
            for(iShd in (iNode * nodePerShd) until (iNode + 1) * nodePerShd)
                bs.set(iShd)
            node2shd[iNode] = bs
        }

        // 2 不能整除的分片部分
        val assigedShdNum = nodePerShd *  nodeNum // 已分配分片数
        // 遍历剩下分片来分配
        var iNode = 0
        for(iShd in assigedShdNum util shardingNum)
            node2shd[iNode++].set(iShd)

        return node2shd;

    }

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每片对应的节点序号
     */
    public override fun sharding(shardingNum: Int, nodeNum: Int): IntArray {
        val shd2Node = IntArray(shardingNum)
        //forEachNodeIndexWithRandomReversed(nodeNum){
        forEachNodeIndexWithRandomOffset(nodeNum){
        }
        return shd2Node
    }

    /**
     * 随机倒序 来处理节点序号
     * @param nodeNum
     * @param actionUtil 处理的回调, 返回false表示停止处理
     */
    public fun forEachNodeIndexWithRandomReversed(nodeNum: Int, actionUtil: (index: Int) -> Boolean): Unit {
        // 随机倒序, 让分片更均衡
        val reversed = randomBoolean()
        // 升序: 从0到nodeNum
        // 倒序: 从nodeNum到0
        var iNode = if(reversed) nodeNum else 0
        // 处理当前节点序号
        while(actionUtil(iNode)){
            // 切换下一个节点序号
            iNode = if(reversed)
                        (iNode - 1 + nodeNum) % nodeNum
                    else
                        (iNode + 1) % nodeNum
        }
    }

    /**
     * 随机偏移 来处理节点序号
     * @param nodeNum
     * @param actionUtil 处理的回调, 返回false表示停止处理
     */
    public fun forEachNodeIndexWithRandomOffset(nodeNum: Int, actionUtil: (index: Int) -> Boolean): Unit {
        // 节点序号的随机偏移
        var iNode = randomInt(nodeNum)
        // 处理当前节点序号
        while(actionUtil(iNode)){
            // 切换下一个节点序号
            iNode = (iNode + 1) % nodeNum
        }
    }

}