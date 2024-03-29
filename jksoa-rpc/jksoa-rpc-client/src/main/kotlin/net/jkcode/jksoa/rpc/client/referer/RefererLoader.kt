package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jksoa.rpc.client.IReferer
import net.jkcode.jksoa.common.loader.ServiceClassLoader

/**
 * 加载服务引用者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object RefererLoader : ServiceClassLoader<Referer>(false) { // 泛型不能再用IReferer(兼容php)

    /**
     * 客户端配置
     */
    public override val config: IConfig = Config.instance("rpc-client", "yaml")

    /**
     * 添加本地服务
     *   直接覆盖
     *
     * @param intf
     * @param service
     * @return
     */
    public fun addLocal(intf: Class<*>, service: Any): Unit{
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
     */
    public override fun createServiceClass(clazz: Class<*>): Referer? {
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
    override fun get(name: String): Referer? {
        // 延迟扫描加载Referer服务
        load()

        return super.get(name)
    }

}