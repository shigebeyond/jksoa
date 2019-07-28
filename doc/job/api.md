# 关键API
Quartz API的关键接口是：

Job - 作业, 代表任务执行
Trigger - 触发器, 负责定时触发作业执行, 可添加/删除job, 代表任务调度

Trigger 代表任务调度, 而Job 代表任务执行, 从而实现任务调度与执行分离, 达到解耦目标

```
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.trigger.CronTrigger

val job = RpcJob(ISimpleService::echo, arrayOf<Any?>("测试消息"))
val trigger = CronTrigger("0/3 * * * * ?")
trigger.addJob(job)
trigger.start()
```


# Job

主要是一个 `execute()` 方法, 负责封装作业的执行逻辑

```
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


当Job的一个trigger被触发时，execute（）方法由trigger的一个工作线程调用。

传递给execute()方法的IJobExecutionContext对象, 向作业实例提供有关其“运行时”信息；

jobAttr 是作业属性, 存储job实例的状态信息


# Trigger

Trigger用于触发Job的执行。当你准备调度一个job时，你创建一个Trigger的实例，然后设置调度相关的属性。

Trigger包含了多个job的jobAttr，用于给Job传递一些触发相关的参数。

jksoa-job自带了各种不同类型的Trigger，最常用的主要是PeriodicTrigger和CronTrigger。

PeriodicTrigger主要用于一次性执行的Job（只在某个特定的时间点执行一次），或者Job在特定的时间点执行，重复执行N次，每次执行间隔T个时间单位。

CronTrigger在基于日历的调度上非常有用，如“每个星期五的正午”，或者“每月的第十天的上午10:15”等。




