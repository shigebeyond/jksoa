package net.jkcode.jksoa.dtx

import net.jkcode.jkmvc.common.ModuleLogSwitcher

// 柔性事务的日志
internal val switcher = ModuleLogSwitcher("dtx")
val dtxLogger = switcher.getLogger("net.jkcode.jksoa.dtx")