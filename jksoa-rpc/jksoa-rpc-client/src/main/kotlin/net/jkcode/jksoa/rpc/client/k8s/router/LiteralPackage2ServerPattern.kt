package net.jkcode.jksoa.rpc.client.k8s.router

/**
 * 常量字符串的包名映射server模式
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class LiteralPackage2ServerPattern(pattern: String, server: String): IPackage2ServerPattern(pattern, server){

    /**
     * 精确度, 字符串越精确度数越高, 用于排序
     */
    override val accuracy: Int
        get() = pattern.length

    /**
     * 检查是否匹配包名
     */
    override fun matchPakcage(pack: String): Boolean {
        return pack.startsWith(pattern)
    }

}