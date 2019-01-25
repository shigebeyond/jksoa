package com.jksoa.job.job

import com.jkmvc.idworker.IIdWorker
import com.jksoa.job.IJob

/**
 * 基础作业
 *   就是简单的实现了id属性
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
abstract class BasicJob(public override val id: Long = idWorker.nextId() /* 作业标识，全局唯一 */) : IJob {

    companion object {

        /**
         * id生成器
         */
        protected val idWorker: IIdWorker = IIdWorker.instance("snowflakeId")

    }

}