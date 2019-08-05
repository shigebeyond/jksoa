# 概述

jksoa一个针对微服务的一系列分布式组件的集合

1. jksoa-rpc: 远程方法调用的组件, 包含 registry / rpc-client / rpc-server 的3个子组件

2. jksoa-job: 分布式任务调度的组件

3. jksoa-guard: 应用守护者组件, 提供了请求合并/流量统计/熔断/限流/降级/缓存等多功能的守护

4. jksoa-tracer: 分布式跟踪的组件, 包含 agent / collector / web 的3个子组件

5. jksoa-mq: 消息队列的组件, 包含, 包含 mq-registry / mq-client / mq-broker 的3个子组件


# 公共组件
1. [插件机制](doc/common/plugin.md)
2. [拦截器机制](doc/common/interceptor.md)
3. [序列器](doc/common/serializer.md)
4. [模块日志](doc/common/log.md)

# jksoa-rpc

远程方法调用的组件

## 基础
[快速开始](doc/rpc/getting_started.md)

### 注册中心
[注册中心](doc/rpc/registry/registry.md)
[url](doc/rpc/registry/url.md)

### rpc-server 服务端
[服务端](doc/rpc/server/server.md)
[多协议支持](doc/rpc/server/protocol.md)
[服务提供者](doc/rpc/server/provider.md)
[server端的请求上下文](doc/rpc/server/context.md)
[异步执行](doc/rpc/server/async-execute.md)
[服务端启动流程](doc/rpc/server/start-flow.md)

### 服务实体
[服务注解](doc/rpc/service/annotation.md)
[服务实例](doc/rpc/service/instance.md)

### rpc-client 客户端

[客户端](doc/rpc/client/client.md)
[服务引用者](doc/rpc/client/referer.md)
[多协议支持](doc/rpc/client/protocol.md)
[异步调用](doc/rpc/client/async-call.md)
[客户端均衡负载](doc/rpc/client/load_balancer.md)
[故障转移(失败重试)](doc/rpc/client/failover.md)
[连接管理](doc/rpc/client/connnection_manage.md)
[连接](doc/rpc/client/connection.md)
[复用单一连接](doc/rpc/client/reuse-connection.md)
[池化的连接的包装器](doc/rpc/client/pooled-connection.md)
[客户端初始化流程](doc/rpc/client/init-flow.md)

### todo
[令牌验证](doc/rpc/todo/token-authorization.md)

## 高级
[架构](doc/rpc/architecture.md)
[rpc流程](doc/rpc/rpc-flow.md)
[附加参数](doc/rpc/common/attachment.md)
[优雅的关机](doc/rpc/common/graceful-shutdown.md)

# jksoa-job

分布式任务调度的组件

## 基础
1. [快速开始](doc/job/getting_started.md)
[作业](doc/job/job.md)
[触发器](doc/job/trigger.md)

## 高级
[架构](doc/job/architecture.md)
[分片策略](doc/job/sharding_strategy.md)
[调度者集群](doc/job/cluster.md)

# jksoa-guard

应用守护者组件, 提供了请求合并/流量统计/熔断/限流/降级/缓存等多功能的守护

## 基础
1. [快速开始](doc/guard/getting_started.md)
2. [注解](doc/guard/annotation.md)

### 合并组件
3. [合并同key请求](doc/guard/key_combiner.md)
4. [合并同group请求](doc/guard/group_combiner.md)
5. [计量器](doc/guard/measure.md)
6. [断路器](doc/guard/circuit_breaker.md)
7. [限流器](doc/guard/rate_limiter.md)
8. [降级](doc/guard/degrade.md)
9. [缓存](doc/guard/cache.md)

## 高级
10. [架构](doc/guard/architecture.md)
11. [方法级守护](doc/guard/method_guard.md)

# jksoa-tracer

分布式跟踪的组件

## 基础
1. [快速开始](doc/tracer/getting_started.md)
2. [agent](doc/tracer/agent.md)
3. [collector](doc/tracer/collector.md)
4. [web](doc/tracer/web.md)

## 高级
5. [架构](doc/tracer/architecture.md)

# jksoa-mq

消息队列的组件

## 基础
1. [快速开始](doc/mq/getting_started.md)

### 消息主体
2. [主题](doc/mq/message/topic.md)
3. [消息](doc/mq/message/message.md)

### 注册中心
4. [注册中心](doc/mq/registry.md)

### producer 生产者
5. [生产者](doc/mq/producer.md)

### consumer 消费者
6. [消费者](doc/mq/consumer/consumer.md)
7. [消息处理器](doc/mq/consumer/handler.md)
8. [重复消费](doc/mq/consumer/duplication.md)
9. [对broker的路由](doc/mq/consumer/route2broker.md)

### broker 中转者
10. [中转者](doc/mq/broker/broker.md)
11. [存储](doc/mq/broker/storage.md)
12. [延迟消息](doc/mq/broker/delay_message.md)
13. [立即同步](doc/mq/broker/immediate_sync.md)

### todo
14. [单主题多队列](doc/mq/todo/topic-queues.md)

## 高级
15. [架构](doc/mq/architecture.md)
16. [消息流转流程](doc/mq/mq-flow.md)
17. [路由](doc/mq/route.md)
18. [有序消息](doc/mq/ordered_message.md)

# 其他
[变更历史](doc/changelog.md)