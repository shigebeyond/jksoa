package net.jkcode.jksoa.common.annotation

import java.lang.reflect.Method

/**
 * 服务方法的元数据的注解
 *    注解中只能使用常量, 故默认值为常量0, 但实际默认值是配置文件中的配置项
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteMethod(
        public val requestTimeoutMillis: Long = 0 /* 请求超时，Long类型，单位毫秒, 如果为0则实际的超时使用rpc-client.yaml中定义的配置项 requestTimeoutMillis */
){
}

/**
 * 服务方法的元数据
 */
public val Method.remoteMethod: RemoteMethod?
    get(){
        return getAnnotation(RemoteMethod::class.java)
    }