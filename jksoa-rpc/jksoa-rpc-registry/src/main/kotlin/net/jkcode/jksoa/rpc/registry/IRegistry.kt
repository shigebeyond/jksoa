package net.jkcode.jksoa.rpc.registry

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.Url


/**
 * 注册中心
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
interface IRegistry: IDiscovery {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IRegistry>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("registry", "yaml")
    }

    /**
     * 注册服务
     *
     * @param url
     * @return
     */
    fun register(url: Url)

    /**
     * 注销服务
     *
     * @param url
     * @return
     */
    fun unregister(url: Url)
}