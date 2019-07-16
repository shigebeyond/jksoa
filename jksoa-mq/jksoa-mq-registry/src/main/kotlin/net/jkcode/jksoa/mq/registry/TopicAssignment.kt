package net.jkcode.jksoa.mq.registry

// topic分配情况: <topic, serverName>, serverName就是协议ip端口
typealias TopicAssignment = MutableMap<String, String>