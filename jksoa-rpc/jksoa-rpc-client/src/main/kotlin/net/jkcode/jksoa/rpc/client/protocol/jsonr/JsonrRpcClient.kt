package net.jkcode.jksoa.rpc.client.protocol.jsonr

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.IRpcClient

/**
 * jsonr协议-rpc客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
open class JsonrRpcClient : IRpcClient {

    /**
     * 连接server
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        try {
            return JsonrConnection(url)
        } catch (e: Exception) {
            throw RpcClientException("客户端创建jsonr连接失败: " + e.message)
        }
    }

    public override fun close() {
    }
}