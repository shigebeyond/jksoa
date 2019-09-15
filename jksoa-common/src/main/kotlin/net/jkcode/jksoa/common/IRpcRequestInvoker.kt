package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons

/**
 * rpc请求的方法调用器
 *   只是方便接口 IInvocation.invoke() 统一调用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-15 5:58 PM
 */
interface IRpcRequestInvoker {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IRpcRequestInvoker>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("rpc-", "yaml")
    }


    /**
     * 调用
     * @param req
     * @return
     */
    fun invoke(req: IRpcRequest): Any?
}