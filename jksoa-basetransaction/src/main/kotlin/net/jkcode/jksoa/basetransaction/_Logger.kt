package net.jkcode.jksoa.basetransaction

import net.jkcode.jkmvc.common.ModuleLogSwitcher
import org.slf4j.LoggerFactory

// 柔性事务的日志
internal val switcher = ModuleLogSwitcher("basetransaction")
val baseTransactionLogger = switcher.getLogger("net.jkcode.jksoa.basetransaction")