package net.jkcode.jksoa.mq.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * mq服务端异常
 */
class MqBrokerException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}