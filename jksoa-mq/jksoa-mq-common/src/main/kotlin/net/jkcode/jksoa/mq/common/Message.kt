package net.jkcode.jksoa.mq.common

import net.jkcode.jkmvc.serialize.ISerializer
import java.io.Serializable
import java.util.*

/**
 * 消息
 *    id在broker端保存时生成, 保证在同一个topic下有序
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:54 PM
 */
data class Message(public val topic: String, // 主题
                   public var data: Any?, // 数据
                   public val groupIds: BitSet = BitSet(), // 分组id
                   public val routeKey: Long = 0 // 路由键, 用于做发送路由与消费路由
): Serializable {

    // 构造函数
    public constructor(topic: String , data: Any?, routeKey: Long): this(topic, data, BitSet(), routeKey)

    // 构造函数
    public constructor(topic: String , data: Any?, group: String, routeKey: Long = 0): this(topic, data, routeKey){
        addGroup(group)
    }

    /**
     * 消息id, 但只在broker端保存时生成, 保证在同一个topic下有序
     *    对producer是无用的, 对broker+consumer有用
     */
    public var id: Long = 0
        protected set

    /**
     * 添加分组
     */
    public fun addGroup(group: String){
        groupIds.set(GroupSequence.get(group))
    }

    /**
     * data要转为 ByteArray, 否则broker在接收消息并反序列化时, 会报错: 找不到类
     *   在producer发给broker前调用, 见 MqProducer.send()
     */
    public fun serializeData(){
        if(data != null)
            data = ISerializer.instance("fst").serialize(data!!)
    }

    /**
     * data要转为 Object, 方便consumer处理
     *    在consumer触发 IMessageHandler.consumeMessages() 前调用, 见 TopicMessagesExecutor.handleRequests()
     */
    public fun unserializeData(){
        if(data != null)
            data = ISerializer.instance("fst").unserialize(data as ByteArray)
    }

    /**
     * 由于id不在data class field中, 因此要重写
     */
    public override fun toString(): String {
        return "Message(id=$id, topic=$topic, data=$data, groupIds=$groupIds, routeKey=$routeKey)"
    }
}