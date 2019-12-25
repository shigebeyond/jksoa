package net.jkcode.jksoa.mq.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * mq客户端异常
 */
open class MqClientException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}