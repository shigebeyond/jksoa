package net.jkcode.jksoa.job

import net.jkcode.jkutil.common.DirtyFlagMap
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
     *   TODO: 在作业执行过后, 如果属性发生变化, 可存储, 以便进程奔溃后重启恢复作业状态
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