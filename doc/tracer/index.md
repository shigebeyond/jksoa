# trace

## 概述
jksoa 是一个分布式跟踪系统

## 特性

- 1、快速接入：使用简单；
- 2、基于拦截器
- 3、复用rpc框架
- 4、通讯方案：基于 NETTY 使用 TCP 协议通讯进行服务调用；
- 5、


## 背景

面对日趋复杂的分布式系统，如服务框架、消息中间件、缓存、数据层等，导致开发人员在业务性能瓶颈定位、故障排除等方面效率低下，没有成熟的Trace工具，需要引入分布式跟踪系统(即Trace系统)。

Trace系统需要能够透明的传递调用上下文，理解系统行为，理清后端调用关系，实现调用链跟踪，调用路径分析，帮助业务人员定位性能瓶颈，排查故障原因等；同时，需要对用户尽量透明，减少对业务代码的侵入性。

设计思想源于Google的论文《Dapper, a Large-Scale Distributed Systems Tracing Infrastructure》。



# 快速入门



## agent端

### agent配置 agent.yaml

```
# agent配置

# 有@TraceableService注解的类所在的包路径
traceableServicePackages: #
  - net.jkcode.jksoa.tracer.agent
```

### 添加agent插件
vim plugin.yaml

```
# rpc客户端的插件
rpcClientPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.RpcClientTracerPlugin
# rpc服务端的插件
rpcServerPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.RpcServerTracerPlugin
# http服务端的插件
httpServerPlugins:
    - net.jkcode.jksoa.tracer.agent.plugin.HttpServerTracerPlugin
```

## collector端

负责提供 ICollectorService 服务, 以供agent调用