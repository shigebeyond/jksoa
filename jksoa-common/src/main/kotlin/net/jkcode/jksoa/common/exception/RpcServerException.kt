package net.jkcode.jksoa.common.exception

/**
 * rpc服务端异常
 */
class RpcServerException : RuntimeException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}