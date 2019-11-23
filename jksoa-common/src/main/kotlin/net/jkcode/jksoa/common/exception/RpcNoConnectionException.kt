package net.jkcode.jksoa.common.exception

import net.jkcode.jkutil.common.JkException

/**
 * rpc时没有连接的异常
 *    在 IConnectionHub.select()/selectAll()中找不到连接时抛出该异常
 */
class RpcNoConnectionException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}