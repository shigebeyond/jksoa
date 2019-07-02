package net.jkcode.jksoa.tracer.common.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * span的标注信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
@org.nustaq.serialization.annotations.Transient
open class Annotation: OrmEntity() {

    companion object {
        // value的值
        public val CLIENT_SEND = "cs"
        public val CLIENT_RECEIVE = "cr"
        public val SERVER_SEND = "ss"
        public val SERVER_RECEIVE = "sr"
        public val EXCEPTION = "ex"
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