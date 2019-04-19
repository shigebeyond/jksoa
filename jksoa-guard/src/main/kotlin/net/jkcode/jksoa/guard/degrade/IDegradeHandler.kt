package net.jkcode.jksoa.guard.degrade

/**
 * 针对方法的降级处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
interface IDegradeHandler {

    /**
     * 处理异常后备
     *
     * @param t
     * @param args 方法调用的参数
     * @return
     */
    fun handleFallback(t: Throwable, args: Array<Any?>): Any?

}