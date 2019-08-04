# 重发

1. 重复生产

producer给broker发送消息时, 会出现网络异常或请求超时等情况, 此时broker可能已收到消息了.

但由于rpc的故障转移(失败重试)的机制, 请求会重发给broker, 此时broker会收到2条重复的消息, 从而导致他给consumer推送了2条重复的消息

2. 重复消费

既然消息重复生产不可避免, 那么就需要在consumer端来处理消息重复消费的问题.

这就要求consumer的`IMessageHandler`的处理要保证幂等