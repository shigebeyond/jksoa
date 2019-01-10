package com.jksoa.client

import com.jksoa.common.IRpcRequest

/**
 * 请求分发者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
interface IRpcRequestDistributor {

    /**
     * 将一个请求发给任一节点
     *
     * @param req 请求
     * @return 响应结果
     */
    fun distributeToAny(req: IRpcRequest): Any?

    /**
     * 将一个请求分配给所有节点
     *
     * @param req 请求
     * @return 多个响应结果
     */
    fun distributeToAll(req: IRpcRequest): Array<Any?>

    /**
     * 分片多个请求
     *   将多个请求分片, 逐片分配给对应的节点
     *
     * @param reqs 多个请求, 请求同一个服务方法
     * @return 多个响应结果
     */
    fun distributeShardings(reqs: Array<IRpcRequest>): Array<Any?>

}