package com.jksoa.protocol

import com.jksoa.common.*

/**
 * 可复用的rpc连接
 *   复用的是同一个server的连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
class ReusableConnection(public override val url: Url /* 服务端地址 */,
                         public override var weight: Int = 1 /* 权重 */
) : IConnection by RecoverableConnection.instance(url.serverUrl) // 根据 serverUrl 来复用 RecoverableConnection 的实例
{
}