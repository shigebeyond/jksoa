package net.jkcode.jksoa.rpc.client.k8s.router

import net.jkcode.jksoa.common.IRpcRequest
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 默认的rpc路由器(解析k8s server)
 *   优先调用自定义的解析器，最后调用 PatternServerResolver
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object RpcRouterContainer: IRpcRouter{

    /**
     * 自定义的解析器
     */
    private val customResolvers: MutableList<IRpcRouter> = CopyOnWriteArrayList()

    /**
     * 解析k8s应用域名(server)
     * @param req
     * @return 协议ip端口(server)
     */
    override fun resovleServer(req: IRpcRequest): String? {
        // 优先调用自定义的解析器
        if(customResolvers.isNotEmpty()) {
            for (r in customResolvers) {
                val server = r.resovleServer(req)
                if(server != null)
                    return server
            }
        }

        // 最后调用 PatternServerResolver
        return PatternRpcRouter.resovleServer(req)
    }

    /**
     * 添加解析器
     */
    public fun addServerResolver(resolver: IRpcRouter): IRpcRouter {
        customResolvers.add(resolver)
        return resolver
    }

    /**
     * 添加解析器
     */
    public fun addServerResolver(resolveFunc: (IRpcRequest)->String?): IRpcRouter {
        val resolver = object :IRpcRouter{
            /**
             * 解析k8s应用域名(server)
             * @param serviceId 服务接口
             * @param reqRouteTag 请求的路由标记
             * @return 协议ip端口(server)
             */
            override fun resovleServer(req: IRpcRequest): String? {
                return resolveFunc(req)
            }

        }
        customResolvers.add(resolver)
        return resolver
    }

    /**
     * 删除解析器
     */
    public fun removeServerResolver(resolver: IRpcRouter) {
        customResolvers.remove(resolver)
    }
}