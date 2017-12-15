package com.jksoa.server

import com.jksoa.common.IServiceClass
import com.jksoa.common.IService
import com.jksoa.common.Url
import java.io.Closeable
import java.lang.reflect.Method

/**
 * 服务提供者
 *
 * @ClassName: IProvider
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
abstract class IProvider : IServiceClass(), Closeable {

    /**
     * 服务路径
     */
    public abstract val serviceUrl: Url

    /**
     * 实现类
     */
    public abstract val clazz: Class<out IService>

    /**
     * 注册服务
     */
    public abstract fun registerService()

    /**
     * 代理服务来执行方法
     *
     * @param method
     * @param args
     * @return
     */
    public abstract fun call(method: Method, args: Array<Any>): Any?

}