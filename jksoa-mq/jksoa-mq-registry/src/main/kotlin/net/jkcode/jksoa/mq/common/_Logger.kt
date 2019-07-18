package net.jkcode.jksoa.mq.common

import net.jkcode.jkmvc.common.ModuleLogSwitcher

/**
 * 主题的正则
 */
public val TopicRegex = "^\\w[\\w\\d_\\.]*$".toRegex()

internal val switcher = ModuleLogSwitcher("mq")
// 注册中心的日志
val mqRegisterLogger = switcher.getLogger("net.jkcode.jksoa.mq.registry")
// 客户端的日志
val mqClientLogger = switcher.getLogger("net.jkcode.jksoa.mq.client")
// 服务端的日志
val mqBrokerLogger = switcher.getLogger("net.jkcode.jksoa.mq.server")

