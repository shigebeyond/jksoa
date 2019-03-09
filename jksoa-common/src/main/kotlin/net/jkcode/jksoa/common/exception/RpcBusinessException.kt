package net.jkcode.jksoa.common.exception

import net.jkcode.jkmvc.common.JkException

/**
 * rpc业务异常
 */
class RpcBusinessException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}