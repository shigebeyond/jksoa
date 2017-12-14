package com.jksoa.common

/**
 * 加载服务提供者
 *
 * @ClassName: IServiceLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
abstract class IServiceLoader {
    /**
     * 添加单个包
     * @param pck 包名
     * @return
     */
    public abstract fun addPackage(pck: String): ServiceLoader

    /**
     * 添加多个包
     * @param pcks 包名
     * @return
     */
    public abstract fun addPackages(pcks: Collection<String>): ServiceLoader

    /**
     * 扫描指定包下的服务提供者
     * @return
     */
    public abstract fun scan(): MutableMap<String, Provider>

    /**
     * 根据服务名来获得服务提供者
     *
     * @param name
     * @return
     */
    public abstract fun getProvider(name: String): Provider?

    /**
     * 根据服务名来获得服务
     *
     * @param name
     * @return
     */
    public fun <T:IService> getService(name: String): T?{
        return getProvider(name)?.ref as T
    }

    /**
     * 根据服务接口来获得服务
     *
     * @param name
     * @return
     */
    public fun <T:IService> getService(intf: Class<T>): T?{
        return getService(intf.name)
    }

    /**
     * 根据服务接口来获得服务
     *
     * @return
     */
    public inline fun <reified T:IService> getService(): T?{
        return getService(T::class.java)
    }
}
