package com.jksoa.job

/**
 * 作业分发者, 负责作业分片与分配分片
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
interface IJobDistributor {

    /**
     * 分发作业
     *   作业分片, 逐片交给对应的节点来处理
     *
     * @param job
     */
    fun distribute(job: Job)
}