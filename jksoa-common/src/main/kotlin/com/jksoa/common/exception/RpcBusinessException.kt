package com.jksoa.common.exception

/**
 * rpc业务异常
 */
class RpcBusinessException : RuntimeException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}