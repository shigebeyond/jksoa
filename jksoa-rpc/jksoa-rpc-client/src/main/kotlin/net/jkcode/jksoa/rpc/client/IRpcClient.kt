package net.jkcode.jksoa.rpc.client

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.Url
import java.io.Closeable

/**
 * rpc协议-客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
interface IRpcClient: Closeable {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IRpcClient>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("rpc-protocol.client", "yaml")
    }

    /**
     * 连接server
     *
     * @param url
     * @return
     */
    fun connect(url: Url): IConnection
}