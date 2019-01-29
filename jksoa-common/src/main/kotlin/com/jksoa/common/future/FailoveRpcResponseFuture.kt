package com.jksoa.common.future

import com.jkmvc.common.Config
import com.jkmvc.future.Callbackable
import com.jkmvc.future.IFutureCallback
import com.jksoa.common.IRpcResponse
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import java.util.concurrent.TimeUnit

/**
 * 失败重试的异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class FailoveRpcResponseFuture(protected val maxTryTimes: Int /* 最大尝试次数 */,
                               protected val responseFactory: (tryTimes: Int) -> IRpcResponseFuture /* 响应工厂方法, 参数是当前尝试次数, 用于发送发送请求 */
) : IRpcResponseFuture, Callbackable<Any?>()  {

    companion object {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")
    }

    /**
     * 异步更新的已尝试次数
     */
    protected var tryTimes: Int = 0

    /**
     * 被代理的目标异步响应对象
     */
    protected var targetResFuture: IRpcResponseFuture = buildResponseFuture()

    /**
     * 响应结果
     */
    public override val result: IRpcResponse?
        get() = targetResFuture.result


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
        clientLogger.debug("-----------------重试第 $tryTimes 次")

        // 2 构建异步响应
        val resFuture = responseFactory(tryTimes)

        // 3 代理回调
        // 3.1 在debug环境下处理早已收到的响应
        // 当client调用本机server时, client很快收到响应
        // 而在debug环境下, 在代码 res.callback = xxx 执行之前就收到响应了, 则设置了回调也无法触发
        if(resFuture.isDone){
            val res = resFuture.result!!
            if (res.exception != null){
                if(tryTimes < maxTryTimes)
                    targetResFuture = buildResponseFuture()
                else
                    callbacks?.forEach {
                        it.failed(res.exception!!)
                    }
            }else {
                callbacks?.forEach {
                    it.completed(res.value)
                }
            }
            return resFuture
        }

        // 3.2 非debug环境, 正常设置回调
        val callback = object : IFutureCallback<Any?> {
            override fun completed(result: Any?) {
                clientLogger.debug("-----------------完成")
                callbacks?.forEach {
                    it.completed(result)
                }
            }

            // 出错重试
            override fun failed(ex: Exception) {
                if(tryTimes < maxTryTimes) {
                    clientLogger.debug("-----------------失败重试")
                    targetResFuture = buildResponseFuture()
                }else {
                    clientLogger.debug("-----------------失败,并超过重试次数 : $tryTimes")
                    callbacks?.forEach {
                        it.failed(ex)
                    }
                }
            }
        }
        resFuture.addCallback(callback)
        return resFuture
    }

    /**
     * 判断任务是否完成
     */
    public override fun isDone(): Boolean {
        return targetResFuture.isDone
    }

    /**
     * 判断任务是否取消
     * @return
     */
    public override fun isCancelled(): Boolean {
        return targetResFuture.isCancelled
    }

    /**
     * 同步获得任务结果, 有默认超时
     */
    public override fun get(): IRpcResponse {
        return get(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): IRpcResponse {
        var ex: Exception? = null
        do{
            val res = targetResFuture.get(timeout, unit)
            // 完成
            if(res.exception == null)
                return res

            // 异常
            ex = res.exception!!
            clientLogger.error("[FailoveRpcResponseFuture.get()]发生异常, 已重试 $tryTimes 次", ex)
        }while(tryTimes < maxTryTimes) // [tryTimes++] is done in [buildResponseFuture()]
        throw ex!!
    }

    /**
     * 取消任务
     * @param mayInterruptIfRunning
     * @return
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return targetResFuture.cancel(mayInterruptIfRunning)
    }

}
