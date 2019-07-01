package net.jkcode.jksoa.tracer.agent

/**
 * 采样器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
interface ISampler {

    /**
     * 是否采样
     * @return
     */
    fun isSample(): Boolean

}
