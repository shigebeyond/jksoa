package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.ModuleLogSwitcher
import org.slf4j.LoggerFactory

// 作业调度的日志
internal val switcher = ModuleLogSwitcher("job")
val jobLogger = switcher.getLogger("net.jkcode.jksoa.job")