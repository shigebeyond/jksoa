package com.jksoa.protocol.rmi

import com.jksoa.client.RefererLoader
import com.jksoa.client.RpcException
import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.Url
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
    public override fun send(req: Request): Response {
        try{
            // 获得referer
            val referer = RefererLoader.get(req.serviceName)
            if(referer == null)
                throw RpcException("服务[${req.serviceName}]没有提供者");

            // 获得方法
            val method = referer.getMethod(req.methodSignature)
            if(method == null)
                throw RpcException("服务方法[${req.serviceName}#${req.methodSignature}]不存在");

            // 调用远程对象的方法
            val value = method.invoke(remoteObject, *req.args)
            return Response(req.id, value)
        }catch (e:Exception){
            return Response(req.id, e)
        }
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        namingContext.close()
    }

}
