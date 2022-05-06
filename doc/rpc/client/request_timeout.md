# 请求超时

请求超时, 指的是client发送rpc请求后, 等待响应的时间, 超过时间直接抛出请求超时错误

其中, 请求超时具体数值的读取逻辑参考 `IRpcRequestMeta.requestTimeoutMillis` 属性值

依次有优先级的读取以下的值:

1. 通过静态函数 `IRpcRequestMeta.setMethodRequestTimeoutMillis()` 设置的超时, client针对某个方法设置的超时
2. 通过服务接口方法的注解属性 `@RemoteMethod.requestTimeoutMillis` 定义的超时, 服务接口类由服务开发者提供, 这代表是服务开发者指定的超时
3. 通过配置文件 `rpc-client` 的属性 `requestTimeoutMillis` 定义的默认超时, client设置的全局超时
