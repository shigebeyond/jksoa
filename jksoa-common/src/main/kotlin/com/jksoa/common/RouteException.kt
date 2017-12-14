package com.jksoa.tests

/**
 * 路由异常
 */
class RouteException : RuntimeException {
    constructor(message: String) : super(message) {
    }

    constructor(cause: Throwable) : super(cause) {
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
    }
}