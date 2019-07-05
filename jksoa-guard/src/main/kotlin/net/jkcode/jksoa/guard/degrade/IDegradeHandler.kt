package net.jkcode.jksoa.guard.degrade

/**
 * 针对方法的降级处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
interface IDegradeHandler {

    /**
     * 是否降级中
     * @return
     */
    fun isAutoDegrading(): Boolean

    /**
     * 处理异常后备
     *
     * @param t 异常. 如果为null则为自动降级, 否则为异常降级
     * @param args 方法调用的参数
     * @return
     */
    fun handleFallback(t: Throwable?, args: Array<Any?>): Any?

}