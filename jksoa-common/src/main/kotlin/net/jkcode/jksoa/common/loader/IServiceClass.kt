package net.jkcode.jksoa.common.loader

import net.jkcode.jkmvc.common.getMethodByName
import net.jkcode.jkmvc.scope.ClosingOnShutdown
import net.jkcode.jkmvc.common.getMethodBySignature
import java.lang.reflect.Method

/**
 * 服务类元数据
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
abstract class IServiceClass: ClosingOnShutdown() {
    /**
     * 接口类
     */
    public abstract val `interface`: Class<*>

    /**
     * 服务标识
     */
    public val serviceId: String
        get() = `interface`.name

    /**
     * 服务实例
     */
    public abstract val service: Any

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public fun getMethod(methodSignature: String): Method?{
        // 1 根据方法签名来查
        if(methodSignature.endsWith(')'))
            return `interface`.getMethodBySignature(methodSignature)

        // 2 根据方法名来查: 忽略参数类型, 一般只用在没有重载的方法中, 方便非java语言(如php)客户端的调用, 不用关心方法签名
        return `interface`.getMethodByName(methodSignature)
    }

}