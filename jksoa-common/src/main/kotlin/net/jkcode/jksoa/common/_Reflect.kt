package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.isSuperClass
import net.jkcode.jksoa.common.annotation.ServiceMeta
import net.jkcode.jksoa.common.annotation.ServiceMethodMeta
import net.jkcode.jksoa.common.exception.RpcClientException
import java.lang.reflect.Method

/**
 * 获得方法的类, 但必须继承了 IService
 * @return
 */
public fun Method.getServiceClass(): Class<out IService> {
    val clazz = declaringClass
    if(IService::class.java.isSuperClass(clazz))
        return clazz as Class<out IService>

    throw RpcClientException("[$this]的类没有继承IService")
}

/**
 * 服务元数据
 */
public val Class<out IService>.serviceMeta: ServiceMeta?
    get(){
        return getAnnotation(ServiceMeta::class.java)
    }

/**
 * 服务方法的元数据
 */
public val Method.serviceMethodMeta: ServiceMethodMeta?
    get(){
        return getAnnotation(ServiceMethodMeta::class.java)
    }