package net.jkcode.jksoa.rpc.client.protocol.jsonr

import com.alibaba.fastjson.JSON
import net.jkcode.jkutil.http.HttpClient
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture

/**
 * jsonr协议的连接
 *    http连接 + json序列化
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class JsonrConnection(url: Url): BaseConnection(url){

    /**
     * http url
     */
    protected val httpUrl: String = "http://${url.host}:${url.port}/"

    /**
     * http client
     */
    protected val httpClient: HttpClient = HttpClient()

    /**
     * 客户端发送请求
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        // 序列化请求
        val json = JSON.toJSONString(req)
        // 发送请求, 并返回异步响应
        return httpClient.post(httpUrl, json)
                .thenApply { resp ->
                    // 反序列化响应
                    val json = resp.responseBody
                    JSON.parseObject(json, RpcResponse::class.java);
                }
    }

    /**
     * 关闭连接
     */
    public override fun close() {
    }

}
