package net.jkcode.jksoa.job.trigger

import com.jkmvc.common.DirtyFlagMap
import com.jkmvc.common.add
import com.jkmvc.common.format
import net.jkcode.jksoa.common.CommonSecondTimer
import net.jkcode.jksoa.common.CommonThreadPool
import net.jkcode.jksoa.job.IJob
import net.jkcode.jksoa.job.ITrigger
import net.jkcode.jksoa.job.jobLogger
import io.netty.util.Timeout
import io.netty.util.TimerTask
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
     * 作业属性
     */
    protected val jobAttrs: MutableMap<Long, DirtyFlagMap<String, Any?>> = HashMap()

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

        jobLogger.debug("下一轮的等待毫秒数: $delayMillis, 当前时间 = " + Date().format() + ", 下一轮时间 = " + Date().add(Calendar.MILLISECOND, delayMillis.toInt()).format())
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
                // 获得作业属性
                val attr = getJobAttr(job.id)
                // 构建执行上下文
                val context = JobExecutionContext(job.id, this)
                // 执行作业, 要处理好异常
                var ex: Exception? = null
                try {
                    jobLogger.debug("${this.javaClass.simpleName}执行作业: $context")
                    job.execute(context)
                }catch (e: Exception){
                    e.printStackTrace()
                    ex = e
                }
                // 更新作业属性
                if(attr.dirty)
                    jobAttrs.put(job.id, attr)

                // TODO: 记录作业执行结果
            }

            // 重复次数+1
            triggerCount++
        }
    }

    /**
     * 获得作业的属性
     * @param jobId
     */
    public override fun getJobAttr(jobId: Long): DirtyFlagMap<String, Any?>{
        return jobAttrs.getOrPut(jobId){
            DirtyFlagMap()
        }!!
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