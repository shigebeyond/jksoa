package com.jksoa.client.protocol.rmi

import com.jksoa.common.Url
import com.jksoa.common.exception.RpcClientException
import com.jksoa.client.IConnection
import com.jksoa.client.IRpcClient

/**
 * rmi协议-客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
open class RmiClient : IRpcClient {

    /**
     * 连接server
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        try {
            return RmiConnection(url)
        } catch (e: Exception) {
            throw RpcClientException("客户端创建rmi连接失败: " + e.message)
        }
    }

    public override fun close() {
    }
}