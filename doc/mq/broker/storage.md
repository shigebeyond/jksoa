# 消息存储

jksoa-mq使用基于 lsmtree 模型的高性能 k/v 存储来存储消息数据

## 消息的存储

类族

```
IMessageRepository
    LsmMessageReader
        LsmMessageWriter
            LsmMessageRepository
```

存储内容:
1. 队列存储: 子目录是queue, key是消息id, value是消息
2. 索引存储: 子目录是index, key是消息id, value是待消费的分组id比特集合
3. 进度存储: 子目录是progress, key为分组id, value是读进度对应的消息id

## 延迟消息的存储

类族

```
IDelayMessageRepository
    LsmDelayMessageRepository
```

存储内容
1. 延迟消息: 子目录是_delayQueue, key是时间戳, value是延迟消息id列表
