package net.jkcode.jksoa.common.loader

import net.jkcode.jkutil.common.getMethodByName
import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.common.getMethodBySignature
import java.io.Closeable
import java.lang.reflect.Method

/**
 * 服务类元数据
 *   从 BaseServiceClass 中抽取 IServiceClass，以便兼容 PhpReferer（不需要 BaseServiceClass.`interface`）
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
interface IServiceClass: Closeable {

    /**
     * 服务标识
     */
    public val serviceId: String

    /**
     * 服务实例
     */
    public val service: Any

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public fun getMethod(methodSignature: String): Method?

}