package com.jksoa.common

import org.slf4j.LoggerFactory

// 注册中心的日志
val registerLogger = LoggerFactory.getLogger("com.jksoa.registry")
// 客户端的日志
val clientLogger = LoggerFactory.getLogger("com.jksoa.client")
// 服务端的日志
val serverLogger = LoggerFactory.getLogger("com.jksoa.server")