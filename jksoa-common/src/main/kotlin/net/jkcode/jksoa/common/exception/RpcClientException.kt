package net.jkcode.jksoa.common.exception

import net.jkcode.jkmvc.common.JkException

/**
 * rpc客户端异常
 */
class RpcClientException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}