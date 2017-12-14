package com.jksoa.server

import com.jkmvc.common.Config
import com.jkmvc.common.getSignature
import com.jksoa.common.IService
import com.jksoa.common.Url
import com.jksoa.registry.IRegistry
import com.jksoa.registry.zk.ZkRegistry
import getIntranetHost
import java.lang.reflect.Method
import java.util.HashMap
import kotlin.collections.ArrayList
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
         * soa配置
         */
        public val config = Config.instance("server", "yaml")

        /**
         * 注册中心
         */
        public val registry: IRegistry = ZkRegistry
    }

    /**
     * 接口类
     */
    public override val interfaces: MutableList<Class<*>> = ArrayList()

    /**
     * 所有方法
     */
    public override val methods: MutableMap<String, Method> = HashMap<String, Method>();

    /**
     * 服务实例
     */
    public lateinit override var service: IService

    init {
        // 创建service实例
        service = clazz.newInstance()

        // 解析接口
        parseInterfaces()

        // 注册服务
        registerService()
    }

    /**
     * 解析接口
     *   遍历服务接口，并注册服务
     */
    private fun parseInterfaces() {
        // 遍历服务接口，并注册服务
        val base = IService::class.java
        for (intf in clazz.interfaces) {
            // 过滤服务接口
            if (intf != base && base.isAssignableFrom(intf)) {
                // 记录接口
                interfaces.add(intf)
            }
        }
    }

    /**
     * 解析方法
     */
    private fun parseMethods() {
        for(intf in interfaces){
            for (method in intf.getMethods()) {
                methods[method.getSignature()] = method
            }
        }
    }

    /**
     * 注册服务
     */
    public override fun registerService(){
        for(intf in interfaces){
            val host = config.getString("host", getIntranetHost())!!
            val url = Url(config["protocol"]!!, host, config["port"]!!, intf.name, config["parameters"]);
            registry.register(url)
        }
    }

    /**
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public override fun  getMethod(methodSignature: String): Method? {
        return methods[methodSignature]
    }

}