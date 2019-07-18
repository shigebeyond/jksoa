package net.jkcode.jksoa.mq.registry

import org.slf4j.LoggerFactory

// mq的日志
val mqLogger = LoggerFactory.getLogger("net.jkcode.jksoa.mq")

/**
 * 主题的正则
 */
public val TopicRegex = "^\\w[\\w\\d_\\.]*$".toRegex()