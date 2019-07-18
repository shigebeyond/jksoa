package net.jkcode.jksoa.mq.registry

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature

// topic分配情况: <topic, serverName>, serverName就是协议ip端口
typealias TopicAssignment = HashMap<String, String>

// 空的topic分配情况
val EmptyTopicAssignment = HashMap<String, String>()

/**
 * 对象转json
 * @return
 */
public fun TopicAssignment.toJson(): String {
    //data.toJSONString()
    return JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat /* Date格式化 */, SerializerFeature.WriteMapNullValue /* 输出null值 */)
}

/**
 * json转topic分配情况
 * @param json
 * @return
 */
public fun json2TopicAssignment(json: String): TopicAssignment {
    return JSON.parseObject(json) as TopicAssignment
}