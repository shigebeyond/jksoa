package com.jksoa.server

import com.jksoa.common.IService
import com.jksoa.common.IServiceClass
import com.jksoa.common.Url
import java.io.Closeable

/**
 * 服务提供者
 *   1 提供服务
 *   2 向注册中心注册服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
abstract class IProvider : IServiceClass() {

    /**
     * 服务路径
     */
    public abstract val serviceUrl: Url

    /**
     * 实现类
     */
    public abstract val clazz: Class<out IService>

}