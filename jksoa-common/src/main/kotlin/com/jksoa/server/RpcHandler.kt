package com.jksoa.server

import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.RouteException

/**
 * Rpc请求处理者
 *
 * @ClassName: RpcHandler
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object RpcHandler : IRpcHandler {

    /**
     * 处理请求
     *
     * @param req
     * @return
     */
    public override fun handle(req: Request): Response {
        try{
            // 获得provider
            val provider = ServiceLoader.getProvider(req.serviceName)
            if(provider == null)
                throw RouteException("服务[${req.serviceName}]没有提供者");

            // 获得方法
            val method = provider.getMethod(req.methodSignature)
            if(method == null)
                throw RouteException("服务方法[${req.serviceName}#${req.methodSignature}]不存在");

            // 调用方法
            val value = method.invoke(provider.ref, req.args)
            return Response(req.id, value)
        }catch (e:Exception){
            return Response(req.id, e)
        }
    }

}