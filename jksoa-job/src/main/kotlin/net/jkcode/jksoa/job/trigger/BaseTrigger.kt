package net.jkcode.jksoa.job.trigger

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkmvc.common.*
import net.jkcode.jkmvc.scope.GlobalAllRequestScope
import net.jkcode.jksoa.job.IJob
import net.jkcode.jksoa.job.ITrigger
import net.jkcode.jksoa.job.jobLogger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * 作业的定时触发器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 2:58 PM
 */
abstract class BaseTrigger : ITrigger {

    /**
     * 当前触发次数
     */
    public override var triggerCount: Int = 0
        protected set

    /**
     * 作业
     */
    public override val jobs: List<IJob> = LinkedList()

    /**
     * 作业执行的上下文
     */
    protected val contexts: MutableMap<Long, JobExecutionContext> = HashMap()

    /**
     * 定时任务
     */
    protected var timeout: Timeout? = null

    /**
     * 添加作业
     */
    public override fun addJob(job: IJob) {
        (jobs as MutableList).add(job)
    }

    /**
     * 准备好下一轮的定时器
     */
    protected fun prepareNextTimeout() {
        // 获得下一轮的等待毫秒数
        val delayMillis = getNextDelayMillis()
        if(delayMillis == null)
            return

        jobLogger.debug("下一轮的等待毫秒数: {}, 当前时间 = {}, 下一轮时间 = {}", delayMillis, Date().format(), Date().add(Calendar.MILLISECOND, delayMillis.toInt()).format())
        // 添加定时器
        CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                // 执行作业
                executeJob()

                // 准备下一轮的定时器
                prepareNextTimeout()
            }
        }, delayMillis, TimeUnit.MILLISECONDS)
    }

    /**
     * 获得下一轮的等待毫秒数
     * @return
     */
    protected abstract fun getNextDelayMillis(): Long?

    /**
     * 执行作业
     */
    protected fun executeJob() {
        // 线程池中执行作业
        CommonThreadPool.execute {
            for(job in jobs){
                // 请求域的开始
                GlobalAllRequestScope.beginScope()

                // 获得作业执行的上下文
                val ctx = contexts.getOrPut(job.id){
                    JobExecutionContext(job.id, this)
                }!!
                // 执行作业, 要处理好异常
                val resFuture = trySupplierFuture {
                    // 执行作业
                    jobLogger.debug("{}执行作业: {}", this.javaClass.simpleName, ctx)
                    job.execute(ctx)

                    // 保存作业属性
                    /*if(ctx.attrs.dirty) {
                        // TODO: 保存 ctx.attrs
                        ctx.attrs.cleanDirty()
                    }*/
                }

                resFuture.whenComplete { r, ex ->
                    // 记录执行异常
                    if(ex != null)
                        job.logExecutionException(ex)

                    // 请求域的结束
                    GlobalAllRequestScope.endScope()
                }
            }

            // 重复次数+1
            triggerCount++
        }
    }

    /**
     * 启动定时器
     */
    public override fun start(){
        // 准备好下一轮的定时器
        prepareNextTimeout()
    }

    /**
     * 停止定时器
     */
    public override fun shutdown(){
        // 删掉定时任务
        timeout?.cancel()
    }
}