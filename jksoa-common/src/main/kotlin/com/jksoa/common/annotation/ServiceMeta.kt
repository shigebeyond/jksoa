package net.jkcode.jksoa.common.annotation

/**
 * 服务元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.CLASS)
annotation class ServiceMeta(public val version: Int = 0 /* 接口版本 */,
                             public val onlyLeader: Boolean = false /* 是否选举leader, 该服务接口只暴露唯一一个选为leader的server */
)
