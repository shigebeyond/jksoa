package net.jkcode.jksoa.common.exception

/**
 * rpc客户端异常
 */
class RpcClientException : RuntimeException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}