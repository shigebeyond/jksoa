package net.jkcode.jksoa.rpc.client.swarm.server

import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * swarm server解析器的集合
 *   优先调用自定义的解析器，最后调用 PatternServerResolver
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object ServerResolverContainer: IServerResolver{

    /**
     * 自定义的解析器
     */
    private val customResolvers: MutableList<IServerResolver> = CopyOnWriteArrayList()

    /**
     * 解析swarm服务名(server)
     * @param serviceId
     * @return 协议ip端口(server)
     */
    override fun resovleServer(serviceId: String): String? {
        // 优先调用自定义的解析器
        if(customResolvers.isNotEmpty()) {
            for (r in customResolvers) {
                val server = r.resovleServer(serviceId)
                if(server != null)
                    return server
            }
        }

        // 最后调用 PatternServerResolver
        return PatternServerResolver.resovleServer(serviceId)
    }

    /**
     * 添加解析器
     */
    public fun addServerResolver(resolver: IServerResolver): IServerResolver {
        customResolvers.add(resolver)
        return resolver
    }

    /**
     * 添加解析器
     */
    public fun addServerResolver(resolveFunc: (String)->String?): IServerResolver {
        val resolver = object :IServerResolver{
            /**
             * 解析swarm服务名(server)
             * @param serviceId
             * @return 协议ip端口(server)
             */
            override fun resovleServer(serviceId: String): String? {
                return resolveFunc(serviceId)
            }

        }
        customResolvers.add(resolver)
        return resolver
    }

    /**
     * 删除解析器
     */
    public fun removeServerResolver(resolver: IServerResolver) {
        customResolvers.remove(resolver)
    }
}