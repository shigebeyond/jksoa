package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkutil.common.JkException

/**
 * tcc事务异常
 */
class TccException : JkException {
    public constructor(message: String) : super(message) {
    }

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}