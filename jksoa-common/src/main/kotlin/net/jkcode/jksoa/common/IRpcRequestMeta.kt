package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.getMethodBySignature
import net.jkcode.jksoa.common.annotation.remoteMethod
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
        public val config = Config.instance("rpc-client", "yaml")
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
            // 默认超时
            val default: Long = config["requestTimeoutMillis"]!!
            try {
                // 方法的注解 @RemoteMethod 定义的超时
                val clazz = Class.forName(this.clazz)
                val method = clazz.getMethodBySignature(this.methodSignature)
                if(method != null && method.remoteMethod != null){
                    val v = method.remoteMethod!!.requestTimeoutMillis
                    if(v != 0L)
                        return v
                }

                return default
            }catch (e: ClassNotFoundException){
                // 在job工程中, 可能直接构造 IRpcRequest, 而没有加载相关的接口类
                return default
            }
        }
}
