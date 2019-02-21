package com.jksoa.job.job.local

import com.jkmvc.common.getSignature
import com.jkmvc.common.toExpr
import com.jksoa.common.CommonThreadPool
import com.jksoa.common.invocation.IShardingInvocation
import com.jksoa.job.IJobExecutionContext
import com.jksoa.job.trigger.BaseTrigger
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 调用bean方法的作业
 *   bean有默认构造函数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
data class ShardingLpcJob(public override val clazz: String, /* 服务接口类全名 */
                          public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                          public override val shardingArgses: Array<Array<*>> /* 分片要调用的实参 */
) : BaseLpcJob(), IShardingInvocation {

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

        // 2 使用线程池来并发处理每个分片
        CommonThreadPool.execute{
            try{
                method.invoke(bean, *shardingArgses[i]) // 调用bean方法
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "ShardingLpcJob: method=$clazz.$methodSignature, shardingSize=$shardingSize, shardingArgses=" + shardingArgses.joinToString(", ", "[", "]"){ args ->
            args.joinToString(", ", "(", ")"){
                it.toExpr()
            }
        }
    }

    /**
     * 转为作业表达式
     * @return
     */
    public override fun toExpr(): String {
        return "shardingLpc " + super<IShardingInvocation>.toExpr()
    }
}