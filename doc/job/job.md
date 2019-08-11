# Job -- 作业

## IJob -- 作业接口

主要是一个 `execute()` 方法, 负责封装作业的执行逻辑

```
package net.jkcode.jksoa.job

/**
 * 作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:06 PM
 */
interface IJob {

    /**
     * 作业标识，全局唯一
     */
    val id: Long

    /**
     * 执行作业
     *
     * @param context 作业执行的上下文
     */
    fun execute(context: IJobExecutionContext)

    /**
     * 转为作业表达式
     * @return
     */
    fun toExpr(): String {
        return "custom " + javaClass.name
    }

    /**
     * 记录作业执行异常
     *   可记录到磁盘中,以便稍后重试
     * @param e
     */
    fun logExecutionException(e: Throwable){
    }

}
```

当Job被一个trigger被触发时，`execute()`方法会扔到trigger的线程池来执行

`execute()`方法有唯一的参数`IJobExecutionContext`类型的对象, 用于向job实例传递执行上下文信息.

## IJobExecutionContext -- 作业执行上下文

用于向job实例提供有关其“运行时”信息:

1. `jobId` -- 作业id
2. `trigger` -- 触发器实例
3. `attrs` -- 作业属性, 用于存储与传递job实例的状态信息, 在`IJob::execute()`实现中可通过读写该属性来维持状态, 譬如可用于构建session

```
package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.DirtyFlagMap
import java.util.*

/**
 * 作业执行的上下文
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 6:39 PM
 */
interface IJobExecutionContext {

    /**
     * 作业标识，全局唯一
     */
    val jobId: Long

    /**
     * 触发器
     */
    val trigger: ITrigger

    /**
     * 作业的属性, 记录当前作业多次执行过程中的状态信息
     */
    val attrs: DirtyFlagMap<String, Any?>

    /**
     * 获得作业属性
     * @param name
     * @return
     */
    fun attr(name: String): Any? {
        return attrs[name]
    }

    /**
     * 设置作业属性
     * @param name
     * @param value
     */
    fun attr(name: String, value: Any?){
        attrs[name] = value
    }
}
```

# 作业分类

类族

```
IJob
    BaseJob
        LambdaJob -- 用lambda包装的作业
        LpcJob -- 调用本地bean方法的作业
        ShardingLpcJob -- 调用本地bean方法的分片作业
        RpcJob -- 发送rpc请求的作业
        ShardingRpcJob -- 发送分片rpc请求的作业
```

按执行者来分类
1. 本地作业, 本地执行, 执行者为本地线程池, 即 Trigger的调度线程池, 包含 LambdaJob/LpcJob/ShardingLpcJob
2. rpc 作业, 通过rpc来执行, 执行者为远程服务提供者节点, 包含 RpcJob/ShardingRpcJob

按是否分片来分类
1. 不分片作业, 包含 `LambdaJob/LpcJob/RpcJob`
2. 分片作业, 包含 `ShardingLpcJob/ShardingRpcJob`

按作业实现方式来分类
1. 用方法调用来实现的作业, 包含 `LpcJob/RpcJob/ShardingLpcJob/ShardingRpcJob`
2. 自定义实现的作业, 包含 `LambdaJob`/直接实现IJob

## 1. LambdaJob -- 用lambda包装的作业
最灵活的作业定义方式, 直接写代码来封装作业逻辑, 但是由于其灵活性, 无法使用cron与作业的复合表达式来表达, 因此不能使用`CronJobLauncher.lauch(cronJobExpr)`来调度作业

只有一个参数, 就是lambda

```
val job = LambdaJob {
    println("测试cron表达式定义的触发器")
}
```

以下的几类作业均是基于调用方法来封装作业逻辑, 不管是本地方法, 还是远程方法, 因此可以使用cron与作业的复合表达式来表达, 因此可以使用`CronJobLauncher.lauch(cronJobExpr)`来调度作业

## 2. LpcJob -- 调用本地bean方法的作业
调用的是本地bean方法: `LocalBean::echo(String)`

有2个参数: 1. 方法引用 2. 实参数组

```
// 调用本地bean方法的作业
val job = LpcJob(LocalBean::echo, arrayOf<Any?>("测试消息"))
```

## 3. ShardingLpcJob -- 调用本地bean方法的分片作业
调用的是本地bean方法: `LocalBean::echo(String)`, 只是加上分片调用

有2个参数: 1. 方法引用 2. 实参数组的数组, 即实参的二维数组, 即每个分片的实参数组

所谓分片调用, 就是按每个分片来调用, 就是以每个分片的的实参数组来调用方法. 

同时由于是本地线程池执行, 因此不用管执行者数目, 直接扔到线程池执行即可

```
// 调用本地bean方法的分片作业
val args:Array<Array<*>> = Array(3) { i ->
    arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
}
val job = ShardingLpcJob(LocalBean::echo, args)
```

## 4. RpcJob -- 发送rpc请求的作业

调用的是远程方法: `ISimpleService::echo(String)`, 实际上是发送rpc请求

有2个参数: 1. 方法引用 2. 实参数组

```
// 发送rpc请求的作业
val job = RpcJob(ISimpleService::echo, arrayOf<Any?>("测试消息"))
```

## 5. ShardingRpcJob -- 发送分片rpc请求的作业
调用的是远程方法: `ISimpleService::echo(String)`, 只是加上分片调用

有2个参数: 1. 方法引用 2. 实参数组的数组, 即实参的二维数组, 即每个分片的实参数组

所谓分片调用, 就是按每个分片来调用, 就是以每个分片的的实参数组来调用方法. 

由于是rpc调用, 执行者即远程服务的提供者. 而分片则是按分片策略分派给这些提供者来执行.

```
// 发送分片rpc请求的作业
val args:Array<Array<*>> = Array(3) { i ->
    arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
}
val job = ShardingRpcJob(ISimpleService::echo, args)
```

作业调度时, 先分片, 后执行, 而分片分派结果的日志输出:

```
分片分派结果, 将 3 个分片分派给 2 个节点:
net.jkcode.jksoa.rpc.client.connection.reuse.ReconnectableConnection(netty://192.168.61.183:9080) => net.jkcode.jkmvc.bit.SetBitIterator(0, 2),
net.jkcode.jksoa.rpc.client.connection.reuse.ReconnectableConnection(netty://192.168.61.184:9080) => net.jkcode.jkmvc.bit.SetBitIterator(1)
```

分片分配结果说明:
1. 有3个分片
2. 有2个节点: 分别是 192.168.61.183:9080 与 192.168.61.184:9080
3. 分片分配结果: 节点 192.168.61.183:9080 得到第0个分片+第2个分片, 节点 192.168.61.184:9080 得到第1个分片
