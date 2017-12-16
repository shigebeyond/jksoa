package com.jksoa.protocol.rmi

import com.jksoa.common.Url
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.IProtocolClient
import com.jksoa.protocol.IProtocolServer
import com.jksoa.server.ServiceException

/**
 * rmi协议-客户端
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
open class RmiProtocolClient: IProtocolClient {
    /**
     * 客户端连接服务器
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        try {
            return RmiConnection(url)
        } catch (e: Exception) {
            throw ServiceException("客户端创建rmi连接失败: " + e.message)
        }
    }
}