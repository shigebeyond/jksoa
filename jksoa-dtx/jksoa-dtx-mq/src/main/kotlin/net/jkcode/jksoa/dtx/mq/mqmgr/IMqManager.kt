package net.jkcode.jksoa.dtx.mq.mqmgr

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import java.util.concurrent.CompletableFuture

/**
 * 消息管理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
interface IMqManager {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IMqManager>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("mq-manager", "yaml")
    }

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @return
     */
    fun sendMq(topic: String, msg: ByteArray): CompletableFuture<Void>

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param handler 消息处理函数
     */
    fun subscribeMq(topic: String, handler: (ByteArray)->Unit)
}