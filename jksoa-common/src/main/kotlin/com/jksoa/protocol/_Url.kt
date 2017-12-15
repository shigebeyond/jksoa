package com.jksoa.protocol

import com.jksoa.common.Url

/**
 * 协议实例
 */
public val Url.protocolInstance: IProtocolServer
    get() = IProtocolClient.instance(this.protocol)

/**
 * 根据url建立连接
 * @return
 */
public fun Url.connect(): IConnection {
    return this.protocolInstance.connect(this)
}