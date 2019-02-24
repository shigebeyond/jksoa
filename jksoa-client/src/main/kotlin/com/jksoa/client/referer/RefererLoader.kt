package com.jksoa.client.referer

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.client.IReferer
import com.jksoa.common.IService
import com.jksoa.common.loader.ServiceClassLoader

/**
 * 加载服务引用者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object RefererLoader : ServiceClassLoader<IReferer>() {

    /**
     * 客户端配置
     */
    override val config: IConfig = Config.instance("client", "yaml")

    /**
     * 添加本地服务
     *   直接覆盖
     *
     * @param intf
     * @param service
     * @return
     */
    public fun addLocal(intf: Class<out IService>, service: IService): Unit{
        val serviceId = intf.name
        val localReferer = Referer(intf, service /* 本地服务 */, true) // 本地服务的引用
        // 直接覆盖
        serviceClasses[serviceId] = localReferer
    }

    /**
     * 创建service类
     *   不重复添加
     *
     * @param clazz 接口类
     * @param registerable 是否注册, 引用者不关心这个
     */
    public override fun createServiceClass(clazz: Class<out IService>, registerable: Boolean): Referer? {
        //去重：对已添加的本地服务，不用再次扫描添加
        if(serviceClasses.containsKey(clazz.name))
            return null

        // 添加远端服务引用
        if(clazz.isInterface) // 接口
            return Referer(clazz) // 服务引用者

        return null
    }

}