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

1. jobId -- 作业id
2. trigger -- 触发器实例
3. triggerCount -- 当前重复次数
4. triggerTime -- 触发时间
5. jobAttr -- 作业属性, 用于存储与传递job实例的状态信息, 在`IJob::execute()`实现中可通过读写该属性来维持状态, 譬如可用于构建session

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
     * 当前重复次数
     */
    val triggerCount: Int
        get() = trigger.triggerCount

    /**
     * 触发时间 = 当前时间
     */
    val triggerTime: Date

    /**
     * 作业的属性
     */
    val jobAttr: DirtyFlagMap<String, Any?>
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
        RpcJob -- 发送分片rpc请求的作业
```

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

## 5. RpcJob -- 发送分片rpc请求的作业
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