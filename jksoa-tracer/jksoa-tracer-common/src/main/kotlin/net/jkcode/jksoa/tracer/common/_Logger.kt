package net.jkcode.jksoa.tracer.common

import net.jkcode.jkmvc.common.ModuleLogSwitcher
import org.slf4j.LoggerFactory

// 跟踪者的日志
internal val switcher = ModuleLogSwitcher("tracer")
val tracerLogger = switcher.getLogger("net.jkcode.jksoa.tracer")