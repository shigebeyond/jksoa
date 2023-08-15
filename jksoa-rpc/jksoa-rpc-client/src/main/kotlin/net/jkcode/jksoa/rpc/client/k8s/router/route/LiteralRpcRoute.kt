package net.jkcode.jksoa.rpc.client.k8s.router

/**
 * 常量字符串的包名映射server的路由
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class LiteralRpcRoute(pattern: String, server: String): IRpcRoute(pattern, server){

    /**
     * 精确度, 字符串越精确度数越高, 用于排序
     */
    override val accuracy: Int
        get() {
            var r =  pattern.length
            if(routeTag != null)
                r += 1000
            return r
        }

    /**
     * 检查是否匹配包名
     */
    override fun matchPakcage(pack: String): Boolean {
        return pack.startsWith(pattern)
    }

}