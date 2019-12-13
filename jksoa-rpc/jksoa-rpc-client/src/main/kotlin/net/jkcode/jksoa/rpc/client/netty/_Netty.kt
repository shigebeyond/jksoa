package net.jkcode.jksoa.rpc.client.netty

import io.netty.buffer.ByteBuf

/**
 * ByteBuf 转字节
 * @return
 */
public fun ByteBuf.toBytes(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}