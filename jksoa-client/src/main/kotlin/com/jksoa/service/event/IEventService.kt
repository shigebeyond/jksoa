package com.jksoa.service.event

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.common.IService
import com.jksoa.common.RpcRequest

/**
 * 事件接口
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IEventService : IService  {

    companion object {
        /**
         * 请求分发者
         */
        protected val distr: IRpcRequestDistributor = RcpRequestDistributor

        /**
         * 广播事件
         * @param event 事件
         */
        public fun broadcastEvent(event: Event){
            // 调用所有节点的 IEventService::notifyEvent(event)
            // 1 构建请求
            val req = RpcRequest(IEventService::notifyEvent, arrayOf<Any?>(event))

            // 2 分发请求
            distr.distributeToAll(req)
        }
    }

    /**
     * 通知事件
     * @param 事件
     */
    fun notifyEvent(event: Event)
}