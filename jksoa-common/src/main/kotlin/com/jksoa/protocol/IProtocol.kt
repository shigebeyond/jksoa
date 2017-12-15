package com.jksoa.protocol

import com.jksoa.server.ProviderLoader

/**
 * rpc协议
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
interface IProtocol {

    companion object{

        /**
         * 根据协议类型来获得协议
         *
         * @param type
         * @return
         */
        public fun instance(type: String): IProtocol {
            return ProtocolType.valueOf(type).protocol
        }
    }

    /**
     * 服役
     */
    fun serve(){
        // 启动服务器
        startServer()
        // 注册服务
        registerServices()
    }

    /**
     * 注册服务
     */
    fun registerServices(){
        // 对每个服务提供者，来注册服务
        for(provider in ProviderLoader.getProviders()){
            provider.registerService()
        }
    }

    /**
     * 启动服务器
     */
    fun startServer(): Unit
}