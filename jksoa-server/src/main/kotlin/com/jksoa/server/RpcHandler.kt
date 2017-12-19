package com.jksoa.server

import com.jksoa.common.Request
import com.jksoa.common.Response

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
     * 处理请求: 调用Provider来处理
     *
     * @param req
     * @return
     */
    public override fun handle(req: Request): Response {
        try{
            // 获得provider
            val provider = ProviderLoader.get(req.serviceId)
            if(provider == null)
                throw ServiceException("服务[${req.serviceId}]没有提供者");

            // 获得方法
            val method = provider.getMethod(req.methodSignature)
            if(method == null)
                throw ServiceException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 调用方法
            val value = method.invoke(provider.service, *req.args)

            return Response(req.id, value)
        }catch (e:Exception){
            return Response(req.id, e)
        }
    }

}