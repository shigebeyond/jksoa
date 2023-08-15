package net.jkcode.jksoa.rpc.client.k8s.router

/**
 * 正则的包名映射server的路由
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class RegexRpcRoute(pattern: String, server: String): IRpcRoute(pattern, server){

    /**
     * 正则
     *   如果模式为 net.jkcode.*, 则正则为 ^net\.jkcode\.[\w\d]+
     */
    public val regex: Regex = ("^" + pattern.replace(".", "\\.")
            .replace("**", "[\\.\\w\\d]+")
            .replace("*", "[\\w\\d]+"))
            .toRegex()

    /**
     * 精确度, 字符串越精确度数越高, 用于排序
     */
    override val accuracy: Int by lazy {
        var r = - pattern.count { it == '*' } // - 正则次数
        if(routeTag != null)
            r += 1000
        r
    }

    /**
     * 检查是否匹配包名
     */
    override fun matchPakcage(pack: String): Boolean {
        return regex.matches(pack)
    }

}