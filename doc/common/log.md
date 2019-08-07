# 日志

框架默认使用 `log4j` 日志技术

# ModuleLogSwitcher -- 组件日志的启用切换器

## 背景

jksoa微服务框架中本项目中有多个组件, 如rpc/job/tracer/mq等, 组件之间会相互依赖, 如mq依赖rpc.

我在开发某个组件时, 只对该组件日志感兴趣, 对其他组件日志不感兴趣, 这就需要禁用这些组件日志.

但一个组件会配置有多个logger(如rpc有register/client/server等几个logger), 禁用该组件日志, 要禁用这多个logger的 `log4j.properties` 配置, 很繁琐

因此, 我设计了`ModuleLogSwitcher`, 直接在组件级别来控制是否启用日志, 如同`开关`, 啪一下就开, 啪一下就关, 很方便, 但只用于jksoa相关组件的开发.

## 配置 module-log-switcher.yaml

每个组件都做成一个开关配置

```
# 按组件来切换启用日志
common: true
rpc: true
job: true
guard: false
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