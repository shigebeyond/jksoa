package com.jksoa.common.exception

/**
 * rpc业务异常
 */
class RpcBusinessException : RuntimeException {
    constructor(message: String) : super(message) {
    }

    constructor(cause: Throwable) : super(cause) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}