package net.jkcode.jksoa.rpc.client.swarm.server

/**
 * 包名映射server模式 - 正则
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class RegexPackage2ServerPattern(pattern: String, server: String): IPackage2ServerPattern(pattern, server){

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
    override val accuracy: Int = - pattern.count { it == '*' } // - 正则次数

    /**
     * 检查是否匹配包名
     */
    override fun isMatch(pack: String): Boolean {
        return regex.matches(pack)
    }

}