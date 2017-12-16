package com.jksoa.client

import com.jksoa.common.Request
import com.jksoa.common.Response
import java.io.Closeable

/**
 * 远程服务中转器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @ClassName: IBroker
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-15 9:25 AM
 */
interface IBroker: Closeable {

    /**
     * 调用远程方法
     *
     * @param req
     * @return
     */
    fun call(req: Request): Response
}