package net.jkcode.jksoa.job

import net.jkcode.jkutil.common.JkException

/**
 * 作业异常
 */
class JobException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}