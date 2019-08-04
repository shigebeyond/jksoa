# Broker -- 消息中转者

消息中转者, 有以下的功能
1. 接收producer发送的消息.
2. 存储消息, 包含消息/分组的读进度
3. 给consumer提供消息, 拉模式或推模式, 推模式下需要维持push consumer的连接

## 子模块

模块图
参考 https://blog.csdn.net/chenaima1314/article/details/79202315

1. service模块: broker的远端服务，处理来自client的请求;
2. repository模块: 负责消息存储, 提供简单的api来在读写消息;
3. consumer connection模块:  负责管理consumer的连接.

## 服务

broker主要是提供rpc服务, 因此broker拥有rpc服务提供者的特性:  如支持集群/服务注册/服务发现

而其中broker提供的涉及到消息读写的服务方法, 都是异步调用, 特别是写方法会异步批处理, 从而提高吞吐量与处理能力.

###  IMqBrokerLeaderService -- 消息中转者的leader服务

主要负责注册主题, 就是给主题分配broker, 并写到注册中心

```
package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.annotation.RemoteService

/**
 * 消息中转者的leader服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@RemoteService(onlyLeader = true)
interface IMqBrokerLeaderService {

    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    fun registerTopic(topic: String): Boolean

    /**
     * 注销主题
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    fun unregisterTopic(topic: String): Boolean

    /**
     * 注册分组
     * @param group 分组
     */
    fun registerGroup(group: String)
}
```

### IMqBrokerService -- 消息中转者服务
```
package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.annotation.RemoteMethod
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.connection.BrokerConnectionHub
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者服务
 *    由于broker server端做了请求(消息)的定时定量处理, 因此请求超时需增大, 详见注解 @RemoteMethod.requestTimeoutMillis
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
@RemoteService(connectionHubClass = BrokerConnectionHub::class)
interface IMqBrokerService {

    /****************** 生产者调用 *****************/
    /**
     * 接收producer发过来的单个消息
     * @param msg 消息
     * @return 消息id
     */
    @RemoteMethod(800)
    fun putMessage(msg: Message): CompletableFuture<Long>

    /**
     * 批量接收producer发过来的多个消息
     *    优化: client在调用前先校验消息是否是同一个主题, 实现见 BrokerConnectionHub.checkBeforePutMessages()
     *
     * @param topic 主题
     * @param msgs 同一个主题的多个消息
     * @return 消息id
     */
    @RemoteMethod(800)
    fun putMessages(topic: String, msgs: List<Message>): CompletableFuture<Array<Long>>

    /****************** 消费者调用 *****************/
    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    fun subscribeTopic(topic: String, group: String): CompletableFuture<Unit>

    /**
     * 接受consumer的拉取消息
     * @param topic 主题
     * @param group 分组
     * @param limit 拉取记录数
     * @return
     */
    fun pullMessagesByGroup(topic: String, group: String, limit: Int = 100): CompletableFuture<List<Message>>

    /**
     * 接受consumer的反馈消息消费结果
     * @param topic 主题
     * @param group 分组
     * @param ids 消息标识
     * @param e 消费异常
     * @return
     */
    @RemoteMethod(800)
    fun feedbackMessages(topic: String, group: String, id: List<Long>, e: Throwable? = null): CompletableFuture<Unit>

}
```

