package com.jksoa.server

import com.jkmvc.common.Config
import com.jkmvc.common.isSuperClass
import com.jksoa.client.Referer
import com.jksoa.client.RefererLoader
import com.jksoa.common.IService
import com.jksoa.common.Url
import com.jksoa.registry.IRegistry
import com.jksoa.registry.zk.ZkRegistry
import getIntranetHost
import java.lang.reflect.Method

/**
 * 服务提供者
 *
 * @ClassName: Provider
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
class Provider(public override val clazz:Class<out IService> /* 实现类 */) : IProvider() {

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
    public override val serviceUrl:Url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!, `interface`.name, config["parameters"]);

    /**
     * 服务实例
     */
    public override val service: IService = clazz.newInstance()

    /**
     * 解析接口
     * @return
     */
    private fun parseInterface(): Class<out IService> {
        // 遍历接口
        return clazz.interfaces.first {
            IService::class.java.isSuperClass(it) // 过滤服务接口
        } as Class<out IService>
    }

    /**
     * 注册服务
     */
    public override fun registerService(){
        // 注册远端服务
        registry.register(serviceUrl)

        // 注册本地服务引用： 对要调用的服务，如果本地有提供，则直接调用本地的服务
        val serviceName = `interface`.name
        val localReferer = Referer(`interface`, service /* 本地服务 */) // 本地服务的引用
        RefererLoader.add(serviceName, localReferer)
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