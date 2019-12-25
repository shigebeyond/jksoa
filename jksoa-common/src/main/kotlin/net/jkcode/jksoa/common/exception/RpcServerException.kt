package net.jkcode.jksoa.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * rpc服务端异常
 */
class RpcServerException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}