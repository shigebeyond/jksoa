package com.jksoa.common

import com.jkmvc.common.Config
import com.jkmvc.common.Url
import com.jkmvc.common.getSignature
import getIntranetHost
import java.lang.reflect.Method
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
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
        public val config = Config.instance("soa", "yaml")

        /**
         * 注册中心
         */
        public val registry: IRegistry = IRegistry.instance(config["registryType"]!!)
    }

    /**
     * 接口类
     */
    public override val interfaces:MutableList<Class<*>> = ArrayList()

    /**
     * 所有方法
     */
    public override val methods: MutableMap<String, Method> = HashMap<String, Method>();

    /**
     * 服务实例
     */
    public lateinit override var ref:IService

    init {
        // 创建service实例
        ref = clazz.newInstance()

        // 解析接口
        parseInterfaces()
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

                // 注册服务
                val host = config.getString("host", getIntranetHost())!!
                val url = Url(config["protocol"]!!, host, config["port"]!!, "", config["parameters"]);
                registry.register(intf.name, url)
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
     * 根据方法签名来获得方法
     *
     * @param methodSignature
     * @return
     */
    public override fun  getMethod(methodSignature: String): Method? {
        return methods[methodSignature]
    }

}