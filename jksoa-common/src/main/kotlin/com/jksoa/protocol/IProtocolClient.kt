package com.jksoa.protocol

import com.jksoa.common.Url

/**
 * rpc协议之客户端部分
 *
 * @ClassName: IProtocolClient
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
interface IProtocolClient {

    companion object{

        /**
         * 根据协议类型来获得协议
         *
         * @param type
         * @return
         */
        public fun instance(type: String): IProtocolServer {
            return ProtocolType.valueOf(type).protocol
        }
    }

    /**
     * 客户端连接服务器
     *
     * @param url
     * @return
     */
    fun connect(url: Url): IConnection
}