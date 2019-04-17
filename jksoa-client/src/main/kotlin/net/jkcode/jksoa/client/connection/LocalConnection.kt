package net.jkcode.jksoa.client.connection

import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.client.referer.RefererLoader
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.common.serverLogger

/**
 * 本地的rpc连接
 *   当且仅当 client端与server端在同一个进程时, 则无需建立真正的连接, 发送请求时直接调用本地服务的方法
 *   TODO: 当 jkmvc 与 jksoa 公用同一个netty server时才考虑支持, 直接改写 ConnectionHub.buildConnection(url): 当url是本地地址时返回的是 LocalConnection 对象
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
class LocalConnection(url: Url): BaseConnection(url){

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRpcRequest): IRpcResponseFuture{
        // 直接调用本地服务的方法
        // 1 获得本地服务
        val referer = RefererLoader.get(req.serviceId) as Referer?
        if(referer == null)
            throw RpcClientException("未加载远程服务: " + req.serviceId)
        if(!referer.local) // 限制本地服务
            throw RpcClientException("没有本地服务: " + req.serviceId)

        // 2 获得方法
        val method = referer.getMethod(req.methodSignature)
        if(method == null)
            throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

        // 3 调用方法
        try {
            val value = method.invoke(referer.service, *req.args)
            serverLogger.debug("Server处理请求：{}，结果: {}", req, value)
            val res = RpcResponse(req.id, value)
            return IRpcResponseFuture.completedFuture(res)
        }catch (e:Exception){
            val res = RpcResponse(req.id, e)
            return IRpcResponseFuture.completedFuture(res)
        }
    }

    /**
     * 关闭连接
     */
    public override fun close() {
    }
}