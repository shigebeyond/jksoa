package net.jkcode.jksoa.common.annotation

import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jkutil.common.getCachedAnnotation
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * 服务元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteService(public val version: Int = 0, // 接口版本
                               public val loadBalancer: String = "" // 均衡负载器类型, 默认是 rpc-client.yaml 中的配置项 loadbalancer,
)

/**
 * 获得服务元数据的注解
 */
public val Class<*>.remoteService: RemoteService?
    get(){
        return getCachedAnnotation()
    }

/**
 * 获得方法的类, 但其接口必须声明注解 @RemoteService
 * @return
 */
public fun Method.getServiceClass(): Class<*> {
    val clazz = declaringClass

    // 接口声明注解 @RemoteService
    val isRemoteService = clazz.remoteService != null
    if(isRemoteService)
        return clazz

    throw RpcClientException("[$this]的接口没有声明注解 @RemoteService")
}