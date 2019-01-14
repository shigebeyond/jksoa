package com.jksoa.server

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.isAbstract
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader

/**
 * 加载服务提供者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object ProviderLoader: ServiceClassLoader<IProvider>() {

    /**
     * 服务端配置
     */
    private val config: IConfig = Config.instance("server", "yaml")

    init {
        // 系统的service包
        addPackage("com.jksoa.service")
        // 用户定义的service包
        addPackages(config["servicePackages"]!!)
    }

    /**
     * 收集service类
     *
     * @param clazz
     * @return
     */
    public override fun collectServiceClass(clazz: Class<IService>): Provider? {
        if (!clazz.isAbstract /* 非抽象类 */ && !clazz.isInterface /* 非接口 */)
            return Provider(clazz) // 服务提供者

        return null
    }

}