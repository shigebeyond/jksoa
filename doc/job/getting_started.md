# 概述
jksoa-job 是一个轻量级分布式作业调度平台，核心的设计理念是简单、轻量、易扩展、高性能。

## 特性

- 1、简单, 易用, 轻量, 易扩展；

- 2、动态：支持动态修改作业状态、启动/停止作业，以及终止运行中作业，即时生效；

- 3、调度者: 同一时间内只有一个调度者, 但是集群模式下有热备, 可保证调度者HA；

- 4、作业执行: 作业执行支持lpc与rpc模式, rpc模式下就是分布式执行，支持集群处理，支持故障转移, 可保证作业执行HA；

- 5、故障转移：依赖于rpc框架的的故障转移, 作业在一台机器上执行失败了, 就切换到另外一台机器上执行。

- 6、动态分片：分片广播作业以执行者为维度进行分片，支持动态扩容执行者集群从而动态增加分片数量，协同进行业务处理；在进行大数据量业务操作时可显著提升作业处理能力和速度。

- 7、全异步：作业调度异步化，依靠异步rpc, 保证高效处理海量作业; 作业执行异步化, 依靠多线程执行, 提高作业执行效率；

## 背景

Quartz作为开源作业调度中的佼佼者，是作业调度的首选。但存在以下问题：

- 1. API设计有点啰嗦, 使用麻烦
- 2. quartz底层以“抢占式”获取DB锁并由抢占成功的节点来执行作业，会导致节点负载悬殊非常大, 同时由抢占失败导致节点空转；而jksoa-job通过调度者主动分配作业给执行者，从而充分发挥集群优势，达到各节点负载均衡。

因此, 本人依赖于jksoa-rpc实现一个更轻量更简单更高效的分布式作业调度系统

# 快速入门

jksoa-job的核心组件有2个：
1. Job - 作业, 代表作业执行
2. Trigger - 触发器, 负责定时触发作业执行, 可添加/删除job, 代表作业调度

## 分别定义 job 与 trigger

```
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.trigger.CronTrigger

// 定义job
val job = RpcJob(ISimpleService::echo, arrayOf<Any?>("测试消息"))
// 定义trigger
val trigger = CronTrigger("0/3 * * * * ?")
// 给trigger添加要触发的job
trigger.addJob(job)
// 启动trigger, 开始定时触发作业
trigger.start()
```

## 使用 `CronJobLauncher` 简化代码

使用`net.jkcode.jksoa.job.cronjob.CronJobLauncher`, 配合cron与作业的复合表达式, 只需要两行代码便可定义定时作业

实际上`CronJobLauncher`是从cron与作业的复合表达式中, 分别解析出 `CronTrigger` 与 `RpcJob`, 效果与上一节是一样的

```
import net.jkcode.jksoa.job.cronjob.CronJobLauncher

// cron与作业的复合表达式, 由cron表达式 + 作业表达式组成, 其中作业表达式前面加`:`, 标识触发的内容是作业
// 如 "0/10 * * * * ? -> lpc net.jkcode.jksoa.rpc.example.SimpleService ping() ()"
//val cronJobExpr = "0/10 * * * * ? -> lpc net.jkcode.jksoa.job.LocalBean echo(String) (\\\"测试消息\\\")"
val cronJobExpr = "0/10 * * * * ? -> rpc net.jkcode.jksoa.rpc.example.ISimpleService echo(String) (\"测试消息\")"
val trigger = CronJobLauncher.lauch(cronJobExpr)
```
