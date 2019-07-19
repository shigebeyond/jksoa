package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.ModuleLogSwitcher

internal val switcher = ModuleLogSwitcher("rpc")
// 注册中心的日志
val registerLogger = switcher.getLogger("net.jkcode.jksoa.registry")
// 客户端的日志
val clientLogger = switcher.getLogger("net.jkcode.jksoa.client")
// 服务端的日志
val serverLogger = switcher.getLogger("net.jkcode.jksoa.server")