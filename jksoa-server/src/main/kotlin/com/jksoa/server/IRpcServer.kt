package com.jksoa.server

import com.jkmvc.common.ClosingOnShutdown
import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.NamedConfiguredSingletons
import com.jksoa.common.Url
import com.jksoa.common.exception.RpcServerException
import com.jksoa.common.serverLogger
import com.jksoa.server.provider.ProviderLoader
import getIntranetHost

/**
 * rpc协议-服务器端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 **/
abstract class IRpcServer : ClosingOnShutdown() {

    // 可配置的单例
    companion object mxx: NamedConfiguredSingletons<IRpcServer>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("protocol.server", "yaml")

        /**
         * 服务端配置
         */
        public val config = Config.instance("server", "yaml")

        /**
         * 当前服务器
         */
        protected lateinit var server: IRpcServer

        /**
         * 获得当前服务器
         */
        @JvmStatic
        public fun current(): IRpcServer {
            return server
        }
    }

    /**
     * 服务器url
     */
    val serverUrl: Url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!)

    /**
     * 服务器名
     */
    val name: String
        get(){
            val clazz = this.javaClass.name
            val i = clazz.lastIndexOf('.')
            return clazz.substring(i + 1)
        }

    /**
     * 启动服务器
     */
    fun start(){
        // 启动服务器
        try{
            serverLogger.info("${name}在地址[$serverUrl]上启动")
            server = this
            doStart() // 可能阻塞，只能在最后一句执行
        }catch(e: Exception){
            serverLogger.error("${name}在地址[$serverUrl]上启动失败", e)
            throw RpcServerException(e)
        }
    }

    /**
     * 注册服务
     */
    fun registerServices(){
        serverLogger.debug("NettyServer调用Provider注册服务")
        // 对每个服务提供者，来注册服务
        for(provider in ProviderLoader.getAll()){
            provider.registerService()
        }
    }

    /**
     * 启动服务器
     *   必须在启动后，主动调用 registerServices() 来注册服务
     */
    abstract fun doStart()
}