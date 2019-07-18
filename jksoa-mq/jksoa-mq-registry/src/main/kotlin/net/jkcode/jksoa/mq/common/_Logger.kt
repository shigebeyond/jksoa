package net.jkcode.jksoa.mq.common

import org.slf4j.LoggerFactory

/**
 * 主题的正则
 */
public val TopicRegex = "^\\w[\\w\\d_\\.]*$".toRegex()

// 注册中心的日志
val mqRegisterLogger = LoggerFactory.getLogger("net.jkcode.jksoa.mq.registry")
// 客户端的日志
val mqClientLogger = LoggerFactory.getLogger("net.jkcode.jksoa.mq.client")
// 服务端的日志
val mqBrokerLogger = LoggerFactory.getLogger("net.jkcode.jksoa.mq.server")

