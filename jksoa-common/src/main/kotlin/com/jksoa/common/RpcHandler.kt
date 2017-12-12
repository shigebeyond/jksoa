package com.jksoa.common

/**
 * Rpc请求处理者
 *
 * @ClassName: RpcHandler
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
class RpcHandler {

    public fun invoke(req: Request): Response{
        // 获得provider
        val provider = ServiceLoader.getService(req.serviceName)
        if(provider == null)
            throw RouteException("不存在服务：${req.serviceName}");

        // 获得方法
        val method = provider.getMethod(req.methodSignature)
        if(method == null)
            throw RouteException("方法");
    }

}