package com.jksoa.client

import com.jkmvc.common.Config
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader

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
    private val config = Config.instance("client", "yaml")

    init{
        // 加载配置的包路径
        val pcks:List<String>? = config["servicePackages"]
        if(pcks != null)
            addPackages(pcks)
    }

    /**
     * 收集service类
     *
     * @param clazz
     */
    public override fun collectServiceClass(clazz: Class<IService>): Referer? {
        return Referer(clazz) // 服务引用者
    }
}