package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.common.invocation.IInvocationMethod

/**
 * rpc调用的元数据
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 4:03 PM
 */
interface IRpcRequestMeta: IInvocationMethod {

    companion object {

        /**
         * 客户端配置
         */
        public val config by lazy{
            Config.instance("client", "yaml")
        }
    }

    /**
     * 版本
     */
    val version: Int

    /**
     * 请求超时
     */
    val requestTimeoutMillis: Long
        get(){
            return config["requestTimeoutMillis"]!!
        }
}
