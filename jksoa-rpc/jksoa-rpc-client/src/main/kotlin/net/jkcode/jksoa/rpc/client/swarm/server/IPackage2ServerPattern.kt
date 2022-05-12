package net.jkcode.jksoa.rpc.client.swarm.server

import net.jkcode.jkutil.common.replaces

/**
 * 包名映射server模式
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
abstract class IPackage2ServerPattern(
        public val pattern: String, // 包名的模式: 用.分割多层包名, *代表一层任意包名, **代表多层任意包名
        public val server: String // server名: 可以带变量, 格式为`$层序号`, 如$0代表第1层包名, $1代表第2层包名, 以此类推
){

    /**
     * 精确度, 字符串越精确度数越高, 用于排序
     */
    public abstract val accuracy: Int

    /**
     * 检查是否匹配包名
     * @param pack 包名
     * @return
     */
    public abstract fun isMatch(pack: String): Boolean

    /**
     * 尝试根据包名来解析swarm服务名(server)的映射
     * @param pack 包名
     * @return
     */
    fun resolveServer(pack: String): String? {
        if(isMatch(pack)){
            if(server.contains('$')) { // 有层变量
                val part = pack.split('.') // 包名分层
                return server.replaces(part, "\\$") // 替换层变量
            }

            // 无层变量
            return server
        }

        return null
    }
}