package net.jkcode.jksoa.mq.consumer.suspend

import net.jkcode.jksoa.mq.common.exception.MqClientException

/**
 * mq拉取消费暂停的异常
 *
 * @author shijianhang
 * @create 2019-8-5 下午6:37
 **/
class MqPullConsumeSuspendException(
        public val suspendSeconds: Int, // 暂停的秒数
        message: String,
        cause: Throwable? = null
) : MqClientException(message) {

    init{
        if(cause != null)
            initCause(cause)
    }

    public constructor(suspendSeconds: Int, cause: Throwable) : this(suspendSeconds, cause.toString(), cause) {
    }

}