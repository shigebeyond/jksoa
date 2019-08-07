# 生产者 -- Producer

就2个方法, 很简单

1. `registerTopic(topic)`

调用远端服务`BrokerLeaderService.registerTopic(topic)`来注册主题

2. `send(msg)`

调用远端服务`BrokerService.putMessage(msg)`来异步生产消息

