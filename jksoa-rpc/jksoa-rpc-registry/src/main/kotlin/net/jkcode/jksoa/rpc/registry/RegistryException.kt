package net.jkcode.jksoa.rpc.registry

import net.jkcode.jkutil.common.JkException

/**
 * 注册异常
 */
class RegistryException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}