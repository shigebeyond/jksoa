package net.jkcode.jksoa.common.annotation

import net.jkcode.jksoa.common.exception.RpcClientException
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
                               public val onlyLeader: Boolean = false, // 是否选举leader, 该服务接口只暴露唯一一个选为leader的server, 同时只有leader server才会创建服务实例, 其他server创建服务代理
                               public val loadBalancer: String = "", // 均衡负载器类型, 默认是 rpc-client.yaml 中的配置项 loadbalancer,
                               public val connectionHubClass: KClass<*> = Void::class // rpc连接集中器的实现类, 用于在服务发现时管理连接, 如果值为 Void::class, 则使用 ConnectionHub::class
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