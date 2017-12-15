package com.jksoa.server

import com.jksoa.common.IService
import com.jksoa.server.IProvider
import com.jksoa.server.Provider

/**
 * 加载服务提供者
 *
 * @ClassName: IServiceLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
interface IProviderLoader {

    /**
     * 根据服务名来获得服务提供者
     *
     * @param name
     * @return
     */
    fun getProvider(name: String): IProvider?

    /**
     * 获得所有的服务提供者
     * @return
     */
    fun getProviders(): Collection<IProvider>
}
