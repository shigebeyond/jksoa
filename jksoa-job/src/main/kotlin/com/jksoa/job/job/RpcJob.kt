package com.jksoa.job.job

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.common.RpcRequest
import com.jksoa.job.IJobExecutionContext

/**
 * 发送rpc请求的作业
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
class RpcJob(protected val req: RpcRequest) : BasicJob(req.id) {

    companion object {
        /**
         * 请求分发者
         */
        protected val distr: IRpcRequestDistributor = RcpRequestDistributor
    }

    /**
     * 执行作业
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        distr.distributeToAny(req)
    }

}