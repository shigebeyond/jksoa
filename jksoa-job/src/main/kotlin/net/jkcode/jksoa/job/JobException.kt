package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.JkException

/**
 * 作业异常
 */
class JobException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}