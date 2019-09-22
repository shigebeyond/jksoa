# 需求

1. ThreadLocal vs 多线程
ThreadLocal 是个好东西, 他在线程中存储相应的状态, 可以通过非参数传递的方式, 来在不同函数实现中传递状态.

常见的状态, 如有事务状态的资源连接(如db连接/redis连接/rabbitmq连接), 如请求上下文, 如tcc事务管理器中的当前事务.

但同时他是限制于一个线程中, 一旦涉及到异步多线程处理, 则会丢失相应的状态. 因此引申出 ThreadLocal 在多线程中的传递.

2. ThreadLocal 的传递
最常见的处理就是将 `Runable`/`Callable`包装一下, 调用时传递特定的 ThreadLocal, 调用后恢复之前的 ThreadLocal, 这种方式因为有恢复因此比较干净, 但只适用于能将处理预先封装为`Runable`/`Callable`的场景, 有形式上的限制.

很多时候在异步处理场景下, 我更喜欢返回 `CompletableFuture` 来代表一个异步的结果, 同时可以设置回调来处理结果, 但是不能像 `Runable`/`Callable` 包装那样干净, 因为你在一个回调的开始传递特定的 ThreadLocal, 但你不能在回调的结束恢复之前的 ThreadLocal, 因为还有后续的回调要用到特定的 ThreadLocal

因此我发明了有作用域的 ThreadLocal, 即`ScopedTransferableThreadLocal`, 来干净的传递 ThreadLocal.

# 有作用域的 ThreadLocal 的类族

```
IScope -- 作用域
	BaseScope
		IRequestScope -- 请求作用域
			GlobalAllRequestScope -- 所有请求的作用域
			GlobalRpcRequestScope -- rpc请求的作用域
			GlobalHttpRequestScope -- http请求的作用域
		ScopedTransferableThreadLocal -- 有作用域的 ThreadLocal
			IRequestScopedTransferableThreadLocal -- 请求域的 ThreadLocal
```

# 作用域

## IScope -- 作用域对象

1. 实现该接口, 必须承诺 `beginScope()`/`endScope()` 会在作用域开始与结束时调用, 一般用于初始化与销毁资源/状态, 以保证作用域内的状态干净.

2. 父作用域的  `beginScope()`/`endScope()`  会自动调用子作用域的  `beginScope()`/`endScope()`

## IRequestScope -- 请求作用域

对应的请求处理器, 承诺在请求处理前后调用其 `beginScope()`/`endScope()`

有以下3类请求作用域:

1. `GlobalAllRequestScope` -- 所有请求的作用域

2. `GlobalRpcRequestScope` -- rpc请求的作用域

3. `GlobalHttpRequestScope` -- http请求的作用域

注意:

目前框架只支持rpc请求与http请求, 均在rpc请求处理器与http请求处理器中实现: 在请求处理前后调用其  `beginScope()`/`endScope()`

但对于非rpc请求与http请求的场景, 我要求开发者自行确定请求的作用域, 并在处理前后调用  `beginScope()`/`endScope()`

如我在 jksoa-job 任务调用框架中的实现, 在`BaseTrigger`处理job的前后调用`GlobalAllRequestScope.beginScope()`与`GlobalAllRequestScope.endScope()`

# 有作用域的 ThreadLocal

## ScopedTransferableThreadLocal -- 有作用域的可传递的 ThreadLocal

1. 实现 Scopable 接口, 标识有作用域, 保证值的创建与删除无误

1.1 在作用域开始时创建, 保证多线程切换作用域时不污染新的作用域

1.2 在作用域结束时删除, 针对 ThreadLocal 逃逸现象, 防止内存泄露

1.3 Scopable的 `beginScope()`/`endScope()` 必须保证被调用

2. 自动刷新与删除

2.1 beginScope()中刷新

2.2 endScope()中删除

3. 切换线程时传输, 参考 `SttlInterceptor`

4. 所有的get()/set()操作必须在作用域内执行, 也就是说必须先调用 beginScope()

## IRequestScopedTransferableThreadLocal -- 请求域的可传递的 ThreadLocal

1. 在对应请求域中有效

2. 对应的请求处理器, 承诺在请求处理前后调用其  `beginScope()`/`endScope()`, 通过作为请求域的子作用域来自动处理