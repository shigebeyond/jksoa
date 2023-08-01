package net.jkcode.jksoa.rpc.client.k8s.server

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.k8s.K8sUtil

/**
 * 根据模式来解析的k8s server解析器，有缓存
 *   从rpc请求(rpc服务类)中,解析出k8s应用域名(server:协议ip端口)
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object PatternServerResolver : IServerResolver {

    /**
     * 包名转为k8s应用域名(server)的映射配置
     * key是包名的模式: 用.分割多层包名, *代表一层任意包名, **代表多层任意包名
     * value是server地址: 可以带变量, 变量格式为`$层序号`, 如$0代表第1层包名, $1代表第2层包名, 以此类推
     */
    public val mappingConfig: Map<String, String> = K8sUtil.config["package2k8sServer"]!!

    /**
     * 映射模式
     */
    public val mappingPatterns = mappingConfig.map { (pattern, server) ->
                                            if (pattern.contains("*")) // 正则
                                                RegexPackage2ServerPattern(pattern, server)
                                            else // 常量字符串
                                                LiteralPackage2ServerPattern(pattern, server)
                                        }.sortedByDescending { it.accuracy } // 按精准度排序

    /**
     * 缓存解析结果: <rpc服务名, 协议ip端口(server)>
     */
    private val resolveCache: MutableMap<String, String> = HashMap()

    /**
     * 解析k8s应用域名(server)
     * @param serviceId
     * @return 协议ip端口(server)
     */
    override fun resovleServer(serviceId: String): String? {
        // 加缓存, 提高性能
        return resolveCache.getOrPut(serviceId){
            doResovleServer(serviceId)
        }
    }

    /**
     * 真正的解析
     * @param serviceClass
     * @return 协议ip端口(server)
     */
    private fun doResovleServer(serviceClass: String): String {
        // 逐个模式解析
        for (pattern in mappingPatterns) {
            val server = pattern.resolveServer(serviceClass)
            if (server != null)
                return server
        }

        throw RpcClientException("无法根据服务类[$serviceClass]定位k8s server")
    }

}