# 1.1 
1. 实现注册中心

2. 实现rpc机制, 支持以下的策略, 如果不满足需求, 可自行扩展策略

2.1 支持多种rpc协议(jkr/jsonr/rmi)

2.2 支持多种序列化协议(原生/fast/kryo/hessian/protostuff)

2.3 支持多种均衡负载策略(随机)

2.4 支持多种分片策略(平均分)

3. 实现server端

3.1 自动扫描service类, 并自动注册到注册中心

3.2 自动关掉空闲连接, 以便节省资源

4. 实现client端

4.1 自动扫描service类, 并生成代理, 记录到 Referer 中

4.2 支持失败重试

4.3 支持自动重连

4.4 支持rpc请求分片调用

4.5 不支持连接池, 因为不需要: 由于netty发送请求是直接扔到队列中的, 建连接池并没有带来啥性能的提高

# 1.2
1. 完善netty连接管理与io事件相关的调试/日志/修复bug

2. 完善各种资源的释放

3. 完成server/client的优雅关机

# 1.3
1. 支持定时作业调度

2. 支持分布式作业调度

# 1.4
1. 支持mq雏形, 基于rpc来发送消息, broker负责将消息存到db中

# 1.5
1. 将包`com.jksoa`重命名为`net.jkcode.jksoa`

2. 支持限流: 支持令牌桶/计数器限流算法, 支持客户端与服务端双向限流, 支持在方法级注解配置

# 1.6
1. 将自研的 Future 类体系, 换为 CompletableFuture

2. 支持请求合并: 可根据key或group来合并请求, 可用于合并cache/rpc请求

3. 支持请求计量

4. 支持熔断

5. 支持降级

6. 添加 MethodGuard 方法级别的守护者, 支持缓存/合并/请求计量/限流/熔断/降级等注解, 并应用到rpc client中

7. 完善日志

8. 支持可继承ThreadLocal的线程池

# 1.7

1. 支持请求拦截器

2. 支持插件机制

3. 添加分布式跟踪组件 tracer, 使用插件方式来动态接入原有的rpc系统

4. 拆分子工程

5. 完善根据统计指标自动降级

# 1.8

1. rpc service声明不在继承IService接口, 直接使用注解@RemoteService

2. 完善均衡负载

3. 完善 @RemoteService 注解, 添加属性 connectionHubClass 支持自定义连接管理, 添加属性 loadBalancer 支持自定义均衡负载

4. rpc支持双工, 详见rpc-client.yaml/rpc-server.yaml中的配置项 duplex

5. 重构mq

5.1 broker负责将消息存到lsmtree文件中, 一个topic下有3个存储对象

5.1.1 队列存储: 子目录是queue, key是消息id, value是消息

5.1.2 索引存储: 子目录是index, key是消息id, value是待消费的分组id比特集合

5.1.3 进度存储: 子目录是progress, key为分组id, value是读进度对应的消息id

5.2 实现mq的注册中心, 支持topic分配信息的保存与分发, 用json格式存储在zookeeper中

5.3 引入broker leader来负责topic的分配

5.4 实现 ConsumerConnectionHub, 用于在broker端管理consumer连接

5.5 实现 BrokerConnectionHub, 用于在client端管理broker连接

5.6 支持消息的多分组消费, 从生成到存储到消费, producer生产消息时指定多分组, broker存储消息时使用BitSet来存储消息的分组, broker支持并发给多分组consumer推送消息, 只有该消息所有分组的consumer都消费完, 才能删除该消息

5.7 抽取 TopicMessagesExecutor 来执行单个主题的消息的消费, 同时继承 UnitRequestQueueFlusher, 通过改写属性 executor 来控制并发或串行执行

5.8 支持有序消息: 1 Message 添加属性 routeKey 来将消息路由到固定的队列与消费者上;  2 添加 SerialSuspendablePullMessageHandler 来支持串行的可暂停的拉模式的消息处理器, 从而保证消费处理是串行

6. 添加序列号生成器: 基于zk的持久顺序节点来实现

7. 优化分片

8. 重构拦截器, 由原来的before()/after()优化为链式包装拦截处理

9. 重构method guard, 支持rpc server

10. 注册中心支持扩展.

11. 添加rabbitmq的连接池

# 1.9

1. rpc server启动支持等待到关闭

2. 支持基于mq的分布式事务

3. 支持tcc的分布式事务
