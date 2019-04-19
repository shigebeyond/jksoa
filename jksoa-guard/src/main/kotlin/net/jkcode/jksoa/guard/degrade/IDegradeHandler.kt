package net.jkcode.jksoa.guard.degrade

/**
 * 针对方法的降级处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
interface IDegradeHandler {

    /**
     * 处理异常回退
     *
     * @param t
     * @param obj 方法调用的对象
     * @param args 方法调用的参数
     * @return
     */
    fun handleFallback(t: Throwable, obj: Any, args: Array<Any?>): Any?

}