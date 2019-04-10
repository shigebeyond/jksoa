package net.jkcode.jksoa.common.future

import net.jkcode.jkmvc.future.CallbackableFuture
import net.jkcode.jksoa.common.IRpcResponse

/**
 * 异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
abstract class BaseRpcResponseFuture: IRpcResponseFuture, CallbackableFuture<IRpcResponse>() {

    /**
     * 任务是否完成
     * @return
     */
    public override fun isDone(): Boolean {
        return result != null
    }

    /**
     * 尝试取消任务
     * @return 是否取消成功
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return false
    }

    /**
     * 任务是否被取消
     * @return
     */
    public override fun isCancelled(): Boolean {
        return false
    }
}
