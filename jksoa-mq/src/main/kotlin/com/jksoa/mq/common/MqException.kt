package com.jksoa.mq.common

/**
 * 消息异常
 */
class MqException : RuntimeException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}