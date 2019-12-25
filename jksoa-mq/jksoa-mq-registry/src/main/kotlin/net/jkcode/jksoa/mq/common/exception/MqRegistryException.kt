package net.jkcode.jksoa.mq.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * 注册异常
 */
class MqRegistryException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}