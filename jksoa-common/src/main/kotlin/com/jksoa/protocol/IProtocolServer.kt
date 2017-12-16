package com.jksoa.protocol

import com.jksoa.server.ProviderLoader


/**
 * rpc协议-服务器端
 *
 * @ClassName: IProtocolServer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 **/
interface IProtocolServer: IProtocolClient {

    /**
     * 启动服务器
     */
    fun startServer(){
        // 启动服务器
        doStartServer()
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
    fun doStartServer(): Unit
}