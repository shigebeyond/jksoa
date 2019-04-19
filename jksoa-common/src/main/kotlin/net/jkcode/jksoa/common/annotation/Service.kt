package net.jkcode.jksoa.common.annotation

import net.jkcode.jkmvc.cache.ICache
import net.jkcode.jksoa.common.IService
import java.lang.annotation.Documented
import java.lang.annotation.RetentionPolicy
import kotlin.reflect.KClass

/**
 * 服务元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service(public val version: Int = 0 /* 接口版本 */,
                         public val onlyLeader: Boolean = false /* 是否选举leader, 该服务接口只暴露唯一一个选为leader的server */
)

/**
 * 获得服务元数据的注解
 */
public val Class<out IService>.service: Service?
    get(){
        return getAnnotation(Service::class.java)
    }
