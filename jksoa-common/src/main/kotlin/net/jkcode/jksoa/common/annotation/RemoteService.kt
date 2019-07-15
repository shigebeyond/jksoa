package net.jkcode.jksoa.common.annotation

import net.jkcode.jksoa.common.exception.RpcClientException
import java.lang.reflect.Method

/**
 * 服务元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteService(public val version: Int = 0 /* 接口版本 */,
                               public val onlyLeader: Boolean = false /* 是否选举leader, 该服务接口只暴露唯一一个选为leader的server */
)

/**
 * 获得服务元数据的注解
 */
public val Class<*>.remoteService: RemoteService?
    get(){
        return getAnnotation(RemoteService::class.java)
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