package net.jkcode.jksoa.rpc.client.k8s.router

import net.jkcode.jkutil.common.replaces

/**
 * 包名映射server的路由
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
abstract class IRpcRoute(
        patternAndTag: String, // 包名模式(用.分割多层包名, *代表一层任意包名, **代表多层任意包名) + 路由标签
        public val server: String // 应用域名(server名): 可以带变量, 变量格式为`$层序号`, 如$0代表第1层包名, $1代表第2层包名, 以此类推
){
    /**
     * 包名的模式: 用.分割多层包名, *代表一层任意包名, **代表多层任意包名
     */
    public lateinit var pattern: String

    /**
     * 路由标签
     */
    public var routeTag: String? = null

    init {
        if('@' in patternAndTag){
            val parts = patternAndTag.split('@')
            pattern = parts[0]
            routeTag = parts[1]
        }else{
            pattern = patternAndTag
        }
    }

    /**
     * 精确度, 字符串越精确度数越高, 用于排序
     */
    public abstract val accuracy: Int

    /**
     * 检查是否匹配包名
     * @param pack 包名
     * @return
     */
    public abstract fun matchPakcage(pack: String): Boolean

    /**
     * 尝试根据包名来解析k8s应用域名(server)的映射
     * @param serviceId 服务名
     * @param reqRouteTag 请求的路由标记
     * @return
     */
    fun resolveServer(serviceId: String, reqRouteTag: String?): String? {
        if((routeTag == null || routeTag == reqRouteTag) // 匹配请求路由标签
            && matchPakcage(serviceId)){ // 匹配请求的包名
            if(server.contains('$')) { // 有层变量
                val parts = serviceId.split('.') // 包名分层
                return server.replaces(parts, "\\$") // 替换层变量
            }

            // 无层变量
            return server
        }

        return null
    }
}