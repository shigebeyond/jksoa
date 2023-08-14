package net.jkcode.jksoa.rpc.client.k8s.router

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.k8s.K8sUtil
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.zkfile.ZkConfig

/**
 * 根据模式来解析的rpc路由器(解析k8s server)，有缓存
 *   从rpc请求(rpc服务类)中,解析出k8s应用域名(server:协议ip端口)
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object PatternRpcRouter : IRpcRouter {

    /**
     * 映射模式
     */
    public lateinit var patterns: List<IPackage2ServerPattern>

    init {
        /**
         * 包名转为k8s应用域名(server)的映射配置
         * key是包名的模式: 用.分割多层包名, *代表一层任意包名, **代表多层任意包名
         * value是server地址: 可以带变量, 变量格式为`$层序号`, 如$0代表第1层包名, $1代表第2层包名, 以此类推
         */
        var config: IConfig
        try {
            config = object : ZkConfig("rpc-router.yaml") {
                override fun handleConfigChange(data: Map<String, Any?>?) {
                    buildPatterns(this)
                }
            }
        }catch (e: Throwable) {
            clientLogger.error("读远程配置异常({}), 自动切换为读本地配置文件", e.message)
            config = Config.instance("rpc-router", "yaml")
        }

        // 映射模式
        buildPatterns(config) // 按精准度排序
    }

    /**
     * 映射模式
     */
    private fun buildPatterns(config: IConfig) {
        patterns = (config.props as Map<String, String>).map { (pattern, server) ->
            if (pattern.contains("*")) // 正则
                RegexPackage2ServerPattern(pattern, server)
            else // 常量字符串
                LiteralPackage2ServerPattern(pattern, server)
        }.sortedByDescending { it.accuracy }
    }


    /**
     * 解析k8s应用域名(server)
     * @param req
     * @return 协议ip端口(server)
     */
    override fun resovleServer(req: IRpcRequest): String?{
        // 获得请求的路由标记
        val reqRouteTag = System.getenv(IRpcRouter.ROUTE_TAG_NAME) ?: req.getAttachment<String>(IRpcRouter.ROUTE_TAG_NAME)
        // 根据包名+路由标记来解析k8s server
        val server = resovleServer(req.serviceId, reqRouteTag)
        if(server == null)
            throw RpcClientException("无法根据服务类[${req.serviceId}]定位k8s server")
        // 像下一个服务传递路由标记
        if(reqRouteTag != null)
            req.putAttachment(IRpcRouter.ROUTE_TAG_NAME, reqRouteTag)
        return fixServer(server)
    }

    /**
     * 解析k8s应用域名(server)
     * @param serviceId 服务接口
     * @param reqRouteTag 请求的路由标记
     * @return 协议ip端口(server)
     */
    fun resovleServer(serviceId: String, reqRouteTag: String?): String? {
        // 逐个模式解析
        for (pattern in patterns) {
            val server = pattern.resolveServer(serviceId, reqRouteTag)
            if (server != null)
                return server
        }

        throw RpcClientException("无法根据服务类[$serviceId]定位k8s server")
    }

    /**
     * 修正server路径
     */
    fun fixServer(server: String): String {
        // 1 自身是`协议://ip:端口`
        if (server.contains("://"))
            return server

        // 2 只有ip，转为`协议://ip:端口`
        return K8sUtil.k8sServer2Url(server).serverAddr
    }

}