package net.jkcode.jksoa.rpc.registry

import net.jkcode.jkmvc.common.JkException

/**
 * 注册异常
 */
class RegistryException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}