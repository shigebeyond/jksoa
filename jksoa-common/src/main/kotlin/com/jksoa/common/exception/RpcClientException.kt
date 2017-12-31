package com.jksoa.common.exception

/**
 * rpc客户端异常
 */
class RpcClientException : RuntimeException {
    constructor(message: String) : super(message) {
    }

    constructor(cause: Throwable) : super(cause) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}