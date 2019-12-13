package net.jkcode.jksoa.rpc.server.netty

import io.netty.channel.Channel
import io.netty.channel.socket.ServerSocketChannel

/**
 * 标识是否server
 * @return
 */
public fun Channel.isServer(): Boolean {
    return this.parent() is ServerSocketChannel
}