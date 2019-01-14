package com.jksoa.common.future

import com.jkmvc.future.CompletedFuture
import com.jksoa.common.IRpcResponse
import com.jksoa.common.RpcResponse
import org.apache.http.concurrent.FutureCallback

/**
 * 已完成的异步响应，没有延迟
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class CompletedRpcResponseFuture(res: RpcResponse): IRpcResponseFuture, CompletedFuture<Any?>(res.value), IRpcResponse by res {
    
    /**
     * 回调
     */
    public override var callback: FutureCallback<Any?>? = null
        set(value: FutureCallback<Any?>?) {
            field = value
            value?.completed(result)
        }
}
