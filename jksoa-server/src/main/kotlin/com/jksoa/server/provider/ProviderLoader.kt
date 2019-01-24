package com.jksoa.server.provider

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.getConstructorOrNull
import com.jkmvc.common.isAbstract
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader
import com.jksoa.common.exception.RpcServerException
import com.jksoa.server.IProvider

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
    override val config: IConfig = Config.instance("server", "yaml")

    /**
     * 创建service类
     *
     * @param clazz
     * @param registerable 是否注册
     * @return
     */
    public override fun createServiceClass(clazz: Class<out IService>, registerable: Boolean): Provider? {
        if (clazz.isAbstract /* 抽象类 */ || clazz.isInterface /* 接口 */) {
            return null
        }

        // 检查 service 类的默认构造函数
        if(clazz.getConstructorOrNull() == null)
            throw RpcServerException("Service Class [$clazz] has no no-arg constructor") // 无默认构造函数

        return Provider(clazz, registerable) // 服务提供者
    }

}