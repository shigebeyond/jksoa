# 日志

框架默认使用 `log4j` 日志技术

# ModuleLogSwitcher -- 组件日志的切换器

## 背景

jksoa微服务框架中有很多的子组件, 如rpc/job/guard/tracer/mq等

每个组件都有几个logger的配置, 很繁琐.

而我在开发某个组件时, 只想看到该组件的日志, 不想看到其他组件的日志. 如果针对其他组件逐个去修改 `log4j.properties` 配置, 这将是一个很繁琐的工作.

因此, 我针对组件级别, 设计了一个日志切换器, 如同`开关`, 啪一下就开, 啪一下就关, 很方便, 但只用于jksoa相关组件的开发.

## 配置

每个组件都做成一个开关配置, 详见 `module-log-switcher.yaml`

```
common: true
rpc: true
job: true
guard: true
tracer: true
mq: true
```

## 使用

```
package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.ModuleLogSwitcher
import org.slf4j.LoggerFactory

// 作业调度的日志
internal val switcher = ModuleLogSwitcher("job")
val jobLogger = switcher.getLogger("net.jkcode.jksoa.job")
```