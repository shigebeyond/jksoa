package com.jksoa.job

import com.jkmvc.common.randomBoolean
import com.jkmvc.common.randomInt

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
        var i = 0
        //collectNodeIndexWithRandomReversed(nodeNum){
        collectNodeIndexWithRandomOffset(nodeNum){
            shd2Node[i++] = it
            i == shardingNum
        }
        return shd2Node
    }

    /**
     * 随机倒序 来收集节点序号
     * @param nodeNum
     * @param collector 收集的回调, 返回true表示收集完成
     */
    public fun collectNodeIndexWithRandomReversed(nodeNum: Int, collector: (index: Int) -> Boolean): Unit {
        // 随机倒序, 让分片更均衡
        val reversed = randomBoolean()
        // 升序: 从0到nodeNum
        // 倒序: 从nodeNum到0
        var iNode = if(reversed) nodeNum else 0
        // 收集当前节点序号
        while(collector(iNode)){
            // 切换下一个节点序号
            iNode = if(reversed)
                        (iNode - 1 + nodeNum) % nodeNum
                    else
                        (iNode + 1) % nodeNum
        }
    }

    /**
     * 随机偏移 来收集节点序号
     * @param nodeNum
     * @param collector 收集的回调, 返回true表示收集完成
     */
    public fun collectNodeIndexWithRandomOffset(nodeNum: Int, collector: (index: Int) -> Boolean): Unit {
        // 节点序号的随机偏移
        var iNode = randomInt(nodeNum)
        // 收集当前节点序号
        while(collector(iNode)){
            // 切换下一个节点序号
            iNode = (iNode + 1) % nodeNum
        }
    }

}