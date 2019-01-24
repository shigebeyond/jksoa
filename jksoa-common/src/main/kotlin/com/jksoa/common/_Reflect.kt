package com.jksoa.common

import com.jkmvc.common.isSubClass
import com.jkmvc.common.isSuperClass
import com.jksoa.common.exception.RpcClientException
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