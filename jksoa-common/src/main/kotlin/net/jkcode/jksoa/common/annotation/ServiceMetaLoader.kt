package net.jkcode.jksoa.common.annotation

/**
 * 方法元数据注解缓存
 *   key为方法签名
 *   value为方法元数据注解
 */
typealias ServiceMethodMetas = MutableMap<String, ServiceMethodMeta>

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-01 2:52 PM
 */
class ServiceMetaLoader {

    /**
     * 服务类元数据注解缓存
     *   key为服务标识，即接口类全名
     *   value为服务类元数据注解+方法元数据注解
     */
    protected val serviceMetas:MutableMap<String, Pair<ServiceMeta, ServiceMethodMetas>> = HashMap()



}