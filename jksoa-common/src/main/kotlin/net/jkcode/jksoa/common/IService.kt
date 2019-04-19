package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.isSuperClass
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.common.exception.RpcClientException
import java.lang.reflect.Method

/**
 * 服务接口
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:58 PM
 */
@Service
interface IService {

}

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