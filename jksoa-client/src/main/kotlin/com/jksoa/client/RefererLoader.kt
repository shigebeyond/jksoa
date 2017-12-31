package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader
import java.lang.reflect.Modifier

/**
 * 加载服务引用者
 *
 * @ClassName:RefererLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object RefererLoader : ServiceClassLoader<IReferer>() {

    /**
     * 服务端配置
     */
    // 父类init()方法要引用config，但子类config尚未初始化，因此不用赋值，而用函数
    //override val config: IConfig = Config.instance("server", "yaml")
    override val config: IConfig
        get() = Config.instance("client", "yaml")

    /**
     * 收集service类
     *
     * @param clazz
     */
    public override fun collectServiceClass(clazz: Class<IService>): Referer? {
        if(Modifier.isInterface(clazz.modifiers)) // 接口
            return Referer(clazz) // 服务引用者

        return null
    }
}