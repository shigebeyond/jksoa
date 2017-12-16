package com.jksoa.protocol

import com.jkmvc.common.Config
import com.jkmvc.common.ConfiguredSingleton
import com.jkmvc.common.IConfig
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
    companion object mxx: ConfiguredSingleton<IProtocolServer>() {
        /**
         * 配置，内容是哈希 <单例名 to 单例类>
         */
        public override val config: IConfig = Config.instance("protocol.server", "yaml")
    }

    /**
     * 启动服务器
     */
    fun start(){
        // 启动服务器
        doStart()
        // 注册服务
        registerServices()
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
     */
    fun doStart(): Unit
}