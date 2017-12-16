package com.jksoa.protocol

import com.jksoa.common.Url

/**
 * 根据url建立连接
 * @return
 */
public fun Url.connect(): IConnection {
    return IProtocolClient.instance(this.protocol).connect(this)
}