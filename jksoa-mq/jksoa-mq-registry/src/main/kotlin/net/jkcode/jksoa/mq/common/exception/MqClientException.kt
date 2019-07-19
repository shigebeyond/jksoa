package net.jkcode.jksoa.mq.common.exception

import net.jkcode.jkmvc.common.JkException

/**
 * mq客户端异常
 */
class MqClientException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}