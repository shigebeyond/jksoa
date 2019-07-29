package net.jkcode.jksoa.rpc.sharding

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import java.util.*

/**
 * 分片策略
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 9:01 AM
 */
interface IShardingStrategy {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IShardingStrategy>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("sharding-strategy", "yaml")
    }

    /**
     * 分片, 将 shardingNum 分成 nodeNum 份
     * @param shardingNum 分片数
     * @param nodeNum 节点数
     * @return 每节点对应的一组分片序号(比特集)
     */
    fun sharding(shardingNum: Int, nodeNum: Int): Array<BitSet>
}