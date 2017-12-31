package com.jksoa.protocol

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.NamedSingleton
import com.jksoa.common.serverLogger
import com.jksoa.common.exception.RpcServerException
import com.jksoa.server.ProviderLoader

/**
 * rpc协议-服务器端
 *
 * @ClassName: IProtocolServer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 **/
interface IProtocolServer {

    // 可配置的单例
    companion object mxx: NamedSingleton<IProtocolServer>() {
        /**
         * 配置，内容是哈希 <单例名 to 单例类>
         */
        public override val config: IConfig = Config.instance("protocol.server", "yaml")
    }

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
        // 服务端配置
        val config = Config.instance("server", "yaml")
        // 获得端口
        val port: Int = config["port"]!!
        // 启动服务器
        try{
            serverLogger.info("${name}在端口${port}上启动")
            doStart(port) // 可能阻塞，只能在最后一句执行
        }catch(e: Exception){
            serverLogger.error("${name}在端口${port}上启动失败: ${e.message}")
            throw RpcServerException(e)
        }
    }

    /**
     * 注册服务
     */
    fun registerServices(){
        // 对每个服务提供者，来注册服务
        for(provider in ProviderLoader.getAll()){
            provider.registerService()
        }
    }

    /**
     * 启动服务器
     *   必须在启动后，主动调用 registerServices() 来注册服务
     *
     * @param port 端口
     */
    fun doStart(port: Int)
}