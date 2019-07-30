package net.jkcode.jksoa.rpc.client.protocol.rmi

import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import javax.naming.InitialContext

/**
 * rmi连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiConnection(url: Url): BaseConnection(url){

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
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        try{
            // 1 获得referer
            val referer = RefererLoader.get(req.serviceId)
            if(referer == null)
                throw RpcClientException("远程服务[${req.serviceId}]没有引用者");

            // 2 获得方法
            val method = referer.getMethod(req.methodSignature)
            if(method == null)
                throw RpcClientException("远程服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 3 调用远程对象的方法
            val value = method.invoke(remoteObject, *req.args)
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
        namingContext.close()
    }

}
