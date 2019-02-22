package com.jksoa.common.loader

import com.jkmvc.closing.ClosingOnShutdown
import com.jkmvc.common.getMethodBySignature
import com.jksoa.common.IService
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
    public abstract val `interface`: Class<out IService>

    /**
     * 服务标识
     */
    public val serviceId: String
        get() = `interface`.name

    /**
     * 服务实例
     */
    public abstract val service: IService

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public fun getMethod(methodSignature: String): Method?{
        return `interface`.getMethodBySignature(methodSignature)
    }

}