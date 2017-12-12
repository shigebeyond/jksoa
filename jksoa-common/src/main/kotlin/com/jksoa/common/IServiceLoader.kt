package com.jksoa.common

/**
 * 加载服务提供者
 *
 * @ClassName: IServiceLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
interface IServiceLoader {
    /**
     * 添加单个包
     * @param pck 包名
     * @return
     */
    fun addPackage(pck: String): ServiceLoader

    /**
     * 添加多个包
     * @param pcks 包名
     * @return
     */
    fun addPackages(pcks: Collection<String>): ServiceLoader

    /**
     * 扫描指定包下的服务提供者
     * @return
     */
    fun scan(): MutableMap<String, Provider>

    /**
     * 获得服务提供者
     *
     * @param name
     * @return
     */
    fun getService(name: String): Provider?
}
