package com.jksoa.job

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.NamedSingleton

/**
 * 作业分片策略
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 9:01 AM
 */
interface IJobShardingStrategy {

    // 可配置的单例
    companion object: NamedSingleton<IJobShardingStrategy>() {
        /**
         * 配置，内容是哈希 <单例名 to 单例类>
         */
        public override val config: IConfig = Config.instance("job_sharding_strategy", "yaml")
    }

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每片对应的节点序号
     */
    fun sharding(shardingNum: Int, nodeNum: Int): IntArray
}