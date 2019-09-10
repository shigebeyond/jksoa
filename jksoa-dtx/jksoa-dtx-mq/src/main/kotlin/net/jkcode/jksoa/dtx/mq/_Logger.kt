package net.jkcode.jksoa.dtx.mq

import net.jkcode.jkmvc.common.ModuleLogSwitcher

// 柔性事务的日志
internal val switcher = ModuleLogSwitcher("dtx")
val dtxMqLogger = switcher.getLogger("net.jkcode.jksoa.dtx.mq")