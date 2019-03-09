package net.jkcode.jksoa.mq.common

import net.jkcode.jkmvc.common.JkException

/**
 * 消息异常
 */
class MqException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}