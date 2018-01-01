package com.jksoa.protocol.rmi

import com.jksoa.client.RefererLoader
import com.jksoa.common.*
import com.jksoa.common.exception.RpcClientException
import com.jksoa.common.future.CompletedResponseFuture
import com.jksoa.common.future.IResponseFuture
import com.jksoa.common.Response
import com.jksoa.protocol.IConnection
import javax.naming.InitialContext

/**
 * rmi链接
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiConnection(url: Url): IConnection(url){

    /**
     * 命名空间
     */
    protected val namingContext = InitialContext()

    /**
     * 远端对象
     */
    protected var remoteObject: Any = namingContext.lookup(url.toString(false))

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRequest): IResponseFuture {
        try{
            // 获得referer
            val referer = RefererLoader.get(req.serviceId)
            if(referer == null)
                throw RpcClientException("远程服务[${req.serviceId}]没有引用者");

            // 获得方法
            val method = referer.getMethod(req.methodSignature)
            if(method == null)
                throw RpcClientException("远程服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 调用远程对象的方法
            val value = method.invoke(remoteObject, *req.args)
            val res = Response(req.id, value)
            return CompletedResponseFuture(res)
        }catch (e:Exception){
            val res = Response(req.id, e)
            return CompletedResponseFuture(res)
        }
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        namingContext.close()
    }

}