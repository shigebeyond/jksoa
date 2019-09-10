package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkmvc.common.ModuleLogSwitcher

// 柔性事务的日志
internal val switcher = ModuleLogSwitcher("dtx")
val dtxTccLogger = switcher.getLogger("net.jkcode.jksoa.dtx.tcc")