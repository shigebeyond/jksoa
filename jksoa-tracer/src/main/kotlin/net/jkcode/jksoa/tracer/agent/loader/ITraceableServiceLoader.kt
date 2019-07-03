package net.jkcode.jksoa.tracer.agent.loader

import net.jkcode.jksoa.tracer.agent.Tracer

/**
 * 可跟踪的服务加载器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 11:58 AM
 */
abstract class ITraceableServiceLoader {

    init {
        Tracer.addServiceLoader(this)
    }

    /**
     * 加载自定义的服务
     *    如web框架中的controller类, 如rpc框架中的service类, 如@TraceableService注解的类
     */
    public abstract fun load(): List<String>

}