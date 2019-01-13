package com.jksoa.sharding

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.NamedSingleton

/**
 * 分片策略
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 9:01 AM
 */
interface IShardingStrategy {

    // 可配置的单例
    companion object: NamedSingleton<IShardingStrategy>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("sharding-strategy", "yaml")
    }

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每片对应的节点序号
     */
    fun sharding(shardingNum: Int, nodeNum: Int): IntArray
}