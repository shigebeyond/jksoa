package net.jkcode.jksoa.common

import org.slf4j.LoggerFactory

// 公用的日志
val commonLogger = LoggerFactory.getLogger("net.jkcode.jksoa.common")
// 注册中心的日志
val registerLogger = LoggerFactory.getLogger("net.jkcode.jksoa.registry")
// 客户端的日志
val clientLogger = LoggerFactory.getLogger("net.jkcode.jksoa.client")
// 服务端的日志
val serverLogger = LoggerFactory.getLogger("net.jkcode.jksoa.server")