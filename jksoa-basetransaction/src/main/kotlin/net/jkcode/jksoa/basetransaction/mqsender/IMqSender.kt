package net.jkcode.jksoa.basetransaction.mqsender

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import java.util.concurrent.CompletableFuture

/**
 * 消息发送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
abstract class IMqSender {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IMqSender>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("mqsender", "yaml")
    }

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @return
     */
    public abstract fun sendMq(topic: String, msg: ByteArray): CompletableFuture<Void>
}