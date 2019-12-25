package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkutil.common.JkException

/**
 * tcc事务异常
 */
class TccException : JkException {

    public constructor(cause: Throwable) : super(cause) {
    }

    public constructor(message: String, cause: Throwable? = null) : super(message, cause) {
    }
}