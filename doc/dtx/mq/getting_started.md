
# 概述
jksoa-dtx-mq 是基于本地消息实现的分布式事务

## 特性
1. 简单, 易用, 轻量, 易扩展；
2. 故障恢复

## 背景
随着网站的访问量与数据量的增加, 我们不可避免的引入数据库拆分和服务拆分, 从而引出了新的分布式事务的问题:
`如何在跨数据库、跨服务的环境下, 保证数据操作的一致性?`

跨应用的业务操作原子性要求，其实是比较常见的。比如在第三方支付成功通知是必须处理的, 而且要处理成功. 可通过mq来实现最终一致性, 也就是说该分布式事务从业务方发起那刻开始, 就确定该事务最终能提交.

# 入门
## 添加依赖
1. gradle
```
compile "net.jkcode.jksoa:jksoa-dtx-mq:1.9.0"
```

2. maven
```
<dependency>
    <groupId>net.jkcode.jksoa</groupId>
    <artifactId>jksoa-dtx-mq</artifactId>
    <version>1.9.0</version>
</dependency>
```

## mq事务配置 dtx-mq.yaml

```
# 基于mq的事务的配置
dbName: default
mqType: rabbitmq # 消息队列类型: rabbitmq / kafka
sendPageSize: 100 # 每次发送的消息数
retrySeconds: 20 # 重发的时间间隔, 单位秒, 为0则不重发
recoverTimerSeconds: !!java.lang.Long 20 # 定时恢复(重发消息)的时间间隔, 为0则不启动定时恢复, 你可以在其他应用中启动
```

说明:
1. `dbName`: tcc事务存储的数据库名: 引用的是 `dataSources.yaml` 中配置的数据库名
2. `mqType`: 消息队列类型: rabbitmq / kafka
3. `sendPageSize`: 每次发送的消息数
4. `retrySeconds`: 恢复机制是对发送失败的消息进行重发, 此项指定消息重发的时间间隔
5. `recoverTimerSeconds`: 定时恢复(重发消息)的时间间隔, 为0则不启动定时恢复, 你可以在其他应用中启动, 启动`net.jkcode.jksoa.dtx.mq.MqTransactionRecovery` 主类即可

## 添加本地消息
使用 `MqTransactionManager.addMq()` 来添加本地消息, 最好是放在业务方的本地事务中, 这样可保证业务数据与消息数据的一致性

同时在本地事务完成后, 会自动发送刚刚添加的消息
```
val db = Db.instance()
// 本地事务
db.transaction {
    // 执行业务sql
    val uid = db.execute("insert into user(name, age) values(?, ?)" /*sql*/, listOf("shi", 1)/*参数*/, "id"/*自增主键字段名，作为返回值*/) // 返回自增主键值
    println("插入user表：" + uid)

    // 添加事务消息
    MqTransactionManager.addMq(topic, "new user: $uid".toByteArray(), "new user", uid.toString())
}
```