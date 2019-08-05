# IMessageHandler 消息处理器

## IMessageHandler -- 消息处理接口

一个属性一个方法
1. `concurrent` -- 是否并发处理
2. `consumeMessages(msgs)` -- 封装消费处理

```
package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
abstract class IMessageHandler(public val concurrent: Boolean = true /* 是否线程池并发执行, 否则单线程串行执行 */) {

    /**
     * 消费处理
     * @param msgs 消息
     */
    public abstract fun consumeMessages(msgs: Collection<Message>)
}
```

## IMessageHandler 的调用

在创建消息处理器`IMessageHandler`时, 就要指定是否并发处理, 即属性 `concurrent`

consumer是使用`TopicMessagesExector`来调用`IMessageHandler`, 他直接将调用扔到`ExecutorService`来执行.

而`ExecutorService`的具体实现是根据`IMessageHandler.concurrent`来确定的:

1. true, 使用线程池实现, 即并发处理
2. false, 使用单线程实现, 即FIFO串行处理

```
/**
 * 改写执行线程(池), 为单线程
 *    一个topic的消息分配到一个线程中串行处理, 从而保证同一个topic下的消息顺序消费
 */
protected override val executor: ExecutorService =
        if(handler.concurrent) // 并发执行
            excutorGroup // 线程池
        else // 串行执行
            excutorGroup.selectExecutor(topic) // 单线程
```
