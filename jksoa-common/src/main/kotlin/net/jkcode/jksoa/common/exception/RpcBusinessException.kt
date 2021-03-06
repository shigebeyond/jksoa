package net.jkcode.jksoa.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * rpc业务异常
 */
class RpcBusinessException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}