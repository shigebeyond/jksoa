package com.jksoa.server

import com.jkmvc.common.Config
import com.jkmvc.common.getSignature
import com.jksoa.common.IService
import com.jksoa.common.Referer
import com.jksoa.common.Url
import com.jksoa.server.IProvider
import com.jksoa.registry.IRegistry
import com.jksoa.registry.zk.ZkRegistry
import getIntranetHost
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.set

/**
 * 服务提供者
 *
 * @ClassName: Provider
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
class Provider(override val clazz:Class<out IService> /* 实现类 */) : IProvider {

    companion object{
        /**
         * 服务端配置
         */
        public val config = Config.instance("server", "yaml")

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = ZkRegistry
    }

    /**
     * 接口类
     */
    public override val `interface`: Class<out IService> = parseInterface()

    /**
     * 服务路径
     */
    public override val serviceUrl:Url = buildServiceUrl()

    /**
     * 所有方法
     */
    public override val methods: MutableMap<String, Method> = parseMethods()

    /**
     * 服务实例
     */
    public override var service: IService = clazz.newInstance()

    /**
     * 解析接口
     * @return
     */
    private fun parseInterface(): Class<out IService> {
        // 遍历接口
        val base = IService::class.java
        return clazz.interfaces.first {
            it != base && base.isAssignableFrom(it) // 过滤服务接口
        } as Class<out IService>
    }

    /**
     * 构建服务路径
     * @return
     */
    private fun buildServiceUrl(): Url {
        val host = config.getString("host", getIntranetHost())!!
        return Url(config["protocol"]!!, host, config["port"]!!, `interface`.name, config["parameters"]);
    }

    /**
     * 解析方法
     * @return
     */
    private fun parseMethods(): HashMap<String, Method> {
        val methods = HashMap<String, Method>();
        for (method in  `interface`.getMethods()) {
            methods[method.getSignature()] = method
        }
        return methods
    }

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public override fun getMethod(methodSignature: String): Method? {
        return methods[methodSignature]
    }

    /**
     * 注册服务
     */
    public override fun registerService(){
        // 注册远端服务
        registry.register(serviceUrl)

        // 注册本地服务引用： 对要调用的服务，如果本地有提供，则直接调用本地的服务
        Referer.addRefer(`interface`, service)
    }

    /**
     * 代理服务来执行方法
     *
     * @param method
     * @param args
     * @return
     */
    public override fun call(method: Method, args: Array<Any>): Any? {
        // TODO: 调用filter
        return method.invoke(service, args)
    }

}