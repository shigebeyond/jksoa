package com.jksoa.common

import com.jkmvc.common.getMethodMaps
import java.lang.reflect.Method

/**
 * 服务类元数据
 *
 * @ClassName: ICaller
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
abstract class IServiceClass {
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
     * 所有方法
     */
    //public val methods: MutableMap<String, Method> = `interface`.getMethodMaps()
    // 由于`interface`属性在子类初始化，递延引用
    public val methods: MutableMap<String, Method> by lazy{
        `interface`.getMethodMaps()
    }

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public fun getMethod(methodSignature: String): Method?{
        return methods[methodSignature]
    }

}