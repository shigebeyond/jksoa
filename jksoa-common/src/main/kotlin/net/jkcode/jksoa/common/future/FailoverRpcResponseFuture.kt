package net.jkcode.jksoa.common.future

import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException

/**
 * 失败重试的异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class FailoverRpcResponseFuture(protected val maxTryTimes: Int /* 最大尝试次数 */,
                                protected val responseFactory: (tryTimes: Int) -> IRpcResponseFuture /* 响应工厂方法, 参数是当前尝试次数, 用于发送发送请求 */
): IRpcResponseFuture(){

    /**
     * 异步更新的已尝试次数
     */
    protected var tryTimes: Int = 0

    /**
     * 被代理的目标异步响应对象
     */
    protected var targetResFuture: IRpcResponseFuture = buildResponseFuture()


    init{
        if(maxTryTimes < 1)
            throw RpcClientException("maxTryTimes must greater than or equals 1")
    }

    /**
     * 构建异步响应 + 更新 tryTimes +　代理回调
     * @return
     */
    protected fun buildResponseFuture(): IRpcResponseFuture {
        // １ 更新 tryTimes: 串行重试, tryTimes++ 线程安全
        tryTimes++
        clientLogger.debug("重试第 {} 次", tryTimes)

        // 2 构建异步响应
        val resFuture = responseFactory(tryTimes)

        // 3 代理回调
        resFuture.exceptionally {
            if(tryTimes < maxTryTimes) {
                clientLogger.debug("失败重试: {}", it.message)
                targetResFuture = buildResponseFuture()
            }else{
                this.completeExceptionally(it)
            }
            null
        }

        resFuture.thenAccept {
            clientLogger.debug("完成")
            this.complete(it)
        }

        return resFuture
    }

}
