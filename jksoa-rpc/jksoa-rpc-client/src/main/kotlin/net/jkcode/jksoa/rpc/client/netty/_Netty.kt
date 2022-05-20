package net.jkcode.jksoa.rpc.client.netty

import io.netty.buffer.ByteBuf
import io.netty.util.AttributeKey
import io.netty.channel.Channel

/**
 * ByteBuf 转字节
 * @return
 */
public fun ByteBuf.toBytes(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}

/**
 * 在Channel中引用NettyConnection的属性名
 */
private val connKey = AttributeKey.valueOf<NettyConnection>("connection")

/**
 * 设置连接：写channel属性
 */
public fun Channel.setConnection(conn: NettyConnection?){
    // 将连接塞到channel的属性中, 以便相互引用
    this.attr<NettyConnection>(connKey).set(conn)
}

/**
 * 获得连接：读channel属性
 */
public fun Channel.getConnection(): NettyConnection?{
    // 从channel的属性中获得连接
    return this.attr<NettyConnection>(connKey).get()
}