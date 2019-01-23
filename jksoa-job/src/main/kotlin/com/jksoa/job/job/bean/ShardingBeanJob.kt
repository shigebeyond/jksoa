package com.jksoa.job.job.bean

import com.jkmvc.common.getOrPutOnce
import com.jkmvc.common.getSignature
import com.jksoa.client.IShardingInvocation
import com.jksoa.job.IJobExecutionContext
import com.jksoa.job.job.BasicJob
import com.jksoa.job.trigger.BaseTrigger
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 调用bean方法的作业
 *   bean有默认构造函数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
class ShardingBeanJob(public override val clazz: String, /* 服务接口类全名 */
                      public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                      public override val shardingArgses: Array<Array<*>> /* 分片要调用的实参 */
) : BasicBeanJob(), IShardingInvocation {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(method: Method, shardingArgses: Array<Array<*>>) : this(method.declaringClass.name, method.getSignature(), shardingArgses)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(func: KFunction<*>, shardingArgses: Array<Array<*>>) : this(func.javaMethod!!, shardingArgses)

    /**
     * 执行作业
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        for (i in 0 until shardingArgses.size)
            execute1Sharding(context, i)
    }

    /**
     * 执行一个分片
     * @param context 作业执行的上下文
     * @param i 分片序号
     */
    protected fun execute1Sharding(context: IJobExecutionContext, i: Int) {
        // 1 如果是最后一个分片, 则在当前线程处理
        if(i == shardingArgses.size - 1){
            method.invoke(bean, *shardingArgses[i]) // 调用bean方法
            return
        }

        // 2 使用trigger的线程池来并发处理每个分片
        (context.trigger as BaseTrigger).executeOtherWork(object : Runnable {
            override fun run() {
                method.invoke(bean, *shardingArgses[i]) // 调用bean方法
            }
        })
    }
}