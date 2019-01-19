package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IService
import com.jksoa.common.ServiceClassLoader

/**
 * 加载服务引用者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object RefererLoader : ServiceClassLoader<IReferer>() {

    /**
     * 服务端配置
     */
    private val config: IConfig = Config.instance("client", "yaml")

    /**
     * 是否已初始化
     */
    private val initialized: Boolean = false

    /**
     * 递延初始化
     *   不能在一开始就初始化
     *   如果当前环境是server时，应先添加本地服务引用，然后再扫描添加远程服务引用
     *   注意去重：对已添加的本地服务，不用再次扫描添加
     */
    private fun initialize(){
        if(!initialized)
            synchronized(this){
                if(!initialized)
                    addPackages(config["servicePackages"]!!)
            }
    }

    /**
     * 添加本地服务
     *
     * @param intf
     * @param service
     * @return
     */
    public fun addLocal(intf: Class<out IService>, service: IService): Unit{
        val serviceId = intf.name
        val localReferer = Referer(intf, service /* 本地服务 */, true) // 本地服务的引用
        serviceClasses[serviceId] = localReferer
    }

    /**
     * 创建service类
     *
     * @param clazz
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

    /**
     * 根据服务标识来获得服务类元数据
     *
     * @param name
     * @return
     */
    public override fun get(name: String): IReferer?{
        initialize() // 递延初始化
        return super.get(name)
    }

    /**
     * 获得所有的服务类元数据
     * @return
     */
    public override fun getAll(): Collection<IReferer> {
        initialize() // 递延初始化
        return super.getAll()
    }

}