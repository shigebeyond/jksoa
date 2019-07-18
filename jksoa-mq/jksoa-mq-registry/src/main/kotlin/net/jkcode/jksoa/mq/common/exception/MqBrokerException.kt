package net.jkcode.jksoa.mq.common.exception

import net.jkcode.jkmvc.common.JkException

/**
 * mq服务端异常
 */
class MqBrokerException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}