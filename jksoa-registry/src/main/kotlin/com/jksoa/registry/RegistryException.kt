package net.jkcode.jksoa.registry

/**
 * 注册异常
 */
class RegistryException : RuntimeException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}