[GitHub](https://github.com/shigebeyond/jksoa) | [Gitee](https://gitee.com/shigebeyond/jksoa) 

# 概述
jksoa是一个微服务组件集合，并适配了k8s集群架构:

1. jksoa-rpc: 远程方法调用的组件, 包含 rpc-k8s-discovery / rpc-client / rpc-server 的3个子组件
2. jksoa-tracer: 分布式跟踪的组件, 包含 agent / collector / web 的3个子组件
3. jksoa-dtx: 分布式事务的组件, 包含 dtx-mq / dtx-tcc(真正的异步非阻塞) 的2个子组件

[性能对比](https://github.com/shigebeyond/jksoa-benchmark)

# 公共组件
1. [插件机制](doc/common/plugin.md)
2. [拦截器机制](doc/common/interceptor.md)
3. [序列器](doc/common/serializer.md)
4. [模块日志](doc/common/log.md)
5. [应用与环境等配置](doc/common/jkapp.md)

# jksoa-rpc
远程方法调用的组件

## 入门
1. [快速开始](doc/rpc/getting_started.md)

### rpc-server 服务端
2. [服务端](doc/rpc/server/server.md)
3. [多协议支持](doc/rpc/server/protocol.md)
4. [服务提供者](doc/rpc/server/provider.md)
5. [server端的请求上下文](doc/rpc/server/context.md)
6. [异步执行](doc/rpc/server/async-execute.md)
7. [服务端启动流程](doc/rpc/server/start-flow.md)

### 服务实体
8. [服务注解](doc/rpc/service/annotation.md)
9. [服务实例](doc/rpc/service/instance.md)

### rpc-client 客户端

10. [客户端](doc/rpc/client/client.md)
11. [多协议支持](doc/rpc/client/protocol.md)
12. [服务引用者](doc/rpc/client/referer.md)
13. [异步调用](doc/rpc/client/async-call.md)
14. [客户端均衡负载](doc/rpc/client/load_balancer.md)
15. [故障转移(失败重试)](doc/rpc/client/failover.md)
16. [连接管理](doc/rpc/client/connnection_manage.md)
17. [连接](doc/rpc/client/connection.md)
18. [复用单一连接](doc/rpc/client/reuse-connection.md)
19. [k8s模式的连接包装器](doc/rpc/client/k8s-connection.md)
20. [客户端初始化流程](doc/rpc/client/init-flow.md)
21. [请求超时](doc/rpc/client/request_timeout.md)
22. [整合jphp-支持php来调用rpc](doc/rpc/client/jphp.md)

## 高级
23. [架构](doc/rpc/architecture.md)
24. [rpc流程](doc/rpc/rpc-flow.md)
25. [附加参数](doc/rpc/common/attachment.md)
26. [优雅的关机](doc/rpc/common/graceful-shutdown.md)

# jksoa-tracer-jaeger实现

使用jaeger来做分布式跟踪

1. [快速开始](doc/tracer-jaeger/getting_started.md)
2. [架构](doc/tracer/architecture.md)

# jksoa-dtx

分布式事务的组件

## 基于本地消息实现的分布式事务

1. [快速开始](doc/dtx/mq/getting_started.md)

## tcc实现的分布式事务

1. [快速开始](doc/dtx/tcc/getting_started.md)
2. [示例](doc/dtx/tcc/demo.md)
3. [架构](doc/dtx/tcc/architecture.md)

# 部署
1. [部署](doc/deploy.md)

# 其他
[部署(CI/CD)](doc/deploy.md)
[变更历史](doc/changelog.md)
