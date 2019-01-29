package com.jksoa.job.job

import com.jkmvc.common.generateId
import com.jksoa.job.IJob

/**
 * 基础作业
 *   就是简单的实现了id属性
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
abstract class BasicJob(public override val id: Long = generateId() /* 作业标识，全局唯一 */) : IJob {

}