package com.jksoa.server

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader
import java.lang.reflect.Modifier

/**
 * 加载服务提供者
 *
 * @ClassName:ProviderLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object ProviderLoader: ServiceClassLoader<IProvider>() {

    /**
     * 服务端配置
     */
    // 父类init()方法要引用config，但子类config尚未初始化，因此不用赋值，而用函数
    //override val config: IConfig = Config.instance("server", "yaml")
    override val config: IConfig
        get() = Config.instance("server", "yaml")

    /**
     * 收集service类
     *
     * @param clazz
     * @return
     */
    public override fun collectServiceClass(clazz: Class<IService>): Provider? {
        val modifiers = clazz.modifiers
        if(!Modifier.isAbstract(modifiers) /* 非抽象类 */ && !Modifier.isInterface(modifiers) /* 非接口 */) {
            return Provider(clazz) // 服务提供者
        }

        return null
    }

}