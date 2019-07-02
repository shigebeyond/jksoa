package net.jkcode.jksoa.tracer.common.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * span的标注信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
open class Annotation: OrmEntity() {

    companion object {
        // value的值
        public val INITIATOR_START = "is" // 发起者开始跟踪
        public val INITIATOR_END = "ie" // 发起者结束跟踪
        public val CLIENT_SEND = "cs" // 客户端发送请求
        public val CLIENT_RECEIVE = "cr" // 客户端收到响应
        public val SERVER_SEND = "ss" // 服务端收到请求
        public val SERVER_RECEIVE = "sr" // 服务端发送响应
        public val EXCEPTION = "ex" // 发生异常
    }

    // 代理属性读写
    public var id:Int by property() //

    public var key:String by property() //

    public var value:String by property() //

    public var ip:String by property() //

    public var port:Int by property() //

    public var timestamp:Long by property() //

    public var duration:Int by property() //

    public var spanId:Long by property() //

    public var traceId:Long by property() //

    public var service:String by property() //

    // initiator start
    public val isIs: Boolean
        get() = key == INITIATOR_START

    // initiator end
    public val isIe: Boolean
        get() = key == INITIATOR_END

    // client send
    public val isCs: Boolean
        get() = key == CLIENT_SEND

    // client receive
    public val isCr: Boolean
        get() = key == CLIENT_RECEIVE

    // server send
    public val isSs: Boolean
        get() = key == SERVER_SEND

    // server receive
    public val isSr: Boolean
        get() = key == SERVER_RECEIVE

    // exception
    public val isEx: Boolean
        get() = key == EXCEPTION
}

val Annotation?.timestamp:Long
    get() = if(this == null) 0 else timestamp