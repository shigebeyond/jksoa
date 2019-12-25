package net.jkcode.jksoa.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * rpc客户端异常
 */
class RpcClientException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}