package net.jkcode.jksoa.common.future

import com.jkmvc.future.CompletedFuture
import com.jkmvc.future.IFutureCallback
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.RpcResponse
import org.apache.http.concurrent.FutureCallback

/**
 * 已完成的异步响应，没有延迟
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class CompletedRpcResponseFuture(res: RpcResponse): IRpcResponseFuture, CompletedFuture<IRpcResponse>(res) {

    /**
     * 响应结果
     */
    public override val result: IRpcResponse
        get() = super<CompletedFuture>.result

    /**
     * 添加回调
     *   立即执行
     * @param
     */
    public override fun addCallback(callback: IFutureCallback<Any?>) {
        callback.completed(result)
    }

}
