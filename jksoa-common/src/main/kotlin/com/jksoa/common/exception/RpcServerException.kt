package com.jksoa.common.exception

/**
 * rpc服务端异常
 */
class RpcServerException : RuntimeException {
    constructor(message: String) : super(message) {
    }

    constructor(cause: Throwable) : super(cause) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}