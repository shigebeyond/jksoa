package com.jksoa.common.future

import com.jkmvc.common.Config
import com.jkmvc.future.Callbackable
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import org.apache.http.concurrent.FutureCallback
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

        // 2 构建异步响应
        val res = responseFactory(tryTimes)

        // 3 代理回调
        // 3.1 在debug环境下处理早已收到的响应
        // 当client调用本机server时, client很快收到响应
        // 而在debug环境下, 在代码 res.callback = xxx 执行之前就收到响应了, 则设置了回调也无法触发
        if(res.isDone){
            if (res.exception != null){
                if(tryTimes < maxTryTimes)
                    targetResFuture = buildResponseFuture()
                else
                    callbacks?.forEach {
                        it.failed(res.exception)
                    }
            }else {
                callbacks?.forEach {
                    it.completed(res.value)
                }
            }
            return res
        }

        // 3.2 非debug环境, 正常设置回调
        val callback = object : FutureCallback<Any?> {
            public override fun cancelled() {
                callbacks?.forEach {
                    it.cancelled()
                }
            }

            public override fun completed(result: Any?) {
                callbacks?.forEach {
                    it.completed(result)
                }
            }

            // 出错重试
            public override fun failed(ex: Exception?) {
                if(tryTimes < maxTryTimes)
                    targetResFuture = buildResponseFuture()
                else
                    callbacks?.forEach {
                        it.failed(ex)
                    }
            }
        }
        res.addCallback(callback)
        return res
    }

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = targetResFuture.requestId
    /**
     * 结果
     */
    public override val value: Any?
        get() = targetResFuture.value
    /**
     * 异常
     */
    public override val exception: Exception?
        get() = targetResFuture.exception

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
    public override fun get(): Any? {
        return get(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        var ex: Exception? = null
        var i = 0
        do{
            println("fuck ${i++}")
            try {
                return targetResFuture.get(timeout, unit)
            }catch(e: Exception){
                // [tryTimes++] is done in [buildResponseFuture()]
                clientLogger.error("Exception [${e.message}] happens in [targetResFuture.get()], And it already try [$tryTimes] times.")
                ex = e
            }
        }while(tryTimes < maxTryTimes)
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
