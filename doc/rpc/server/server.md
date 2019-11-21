# server

目前josoa-rpc支持有2种通讯协议:

1. rmi协议

2. jkr协议, 默认使用这种协议

3. jsonr协议, 使用http协议通讯, 使用json来序列化请求与响应, 多用于跨语言调用

因此导致server也有两种实现

1. `RmiRpcServer`, 实现rmi协议

2. `JkrRpcServer`, 实现jkr协议, 默认协议

其类族如下

```
IRpcServer
	JkrRpcServer
	RmiRpcServer
```

# 启动server

使用 `net.jkcode.jksoa.rpc.server.RpcServerLauncher` 作为主类, 其`main()`方法会启动server

实际上他的实现很简单, 就是根据 `rpc-server.yaml` 配置文件中指定的 `protocol` 协议去调用对应的 `IRpcServer` 的实现类

## 配置 rpc-server.yaml

关键配置项

1. protocol -- 协议, 据此来启动对应的 `IRpcServer` 的实现类
2. host -- ip, 可以不指定, 默认取值 `getIntranetHost()`, 即`192.168`开头的局域网ip
3. port -- 端口, 开放端口来接收rpc服务
4. parameters -- 注册的url参数, 会生成query string, 并存到注册中心对应server节点中
5. servicePackages -- 要提供的服务类所在的包路径
6. netty -- netty启动参数

```
# 服务端配置
duplex: true # 是否双工, 就是双向rpc, 就是server也可以调用client, 但是client不在注册中心注册
protocol: jkr # 协议
#host: 192.168.0.17 # ip
port: 9080 # 端口
parameters: # 参数
  weight: 1
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
    - net.jkcode.jksoa.tracer.collector.service # tracer组件的collector服务
    - net.jkcode.jksoa.mq.broker.service # mq组件的broker服务
registering: true # 是否注册到注册中心
# netty启动参数
netty:
    keepAlive: true # 保持心跳
    reuseAddress: true # 重用端口
    tcpNoDelay: true # 禁用了Nagle算法,允许小包的发送
    soLinger: 5 # 当网卡收到关闭连接请求后，等待 soLinger 秒, 如果期间发送完毕，正常四次挥手关闭连接，否则发送RST包关闭连接
    backlog: 1024 # TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128*8
    sendBufferSize: 65536 # 发送的缓存大小, 默认64K=1024*64
    receiveBufferSize: 65536 # 接收的缓冲大小, 默认64K=1024*64
    acceptorThreadNum: 1 # acceptor线程数
    ioThreadNum: 0 # IO线程数, 如为0, 则为Runtime.getRuntime().availableProcessors()
    handleRequestInIOThread: true # 请求处理是否放到IO线程执行, 否则放到公共线程池中执行
    # IdleStateHandler 中channel空闲检查的时间配置
    readerIdleTimeSecond: 600
    writerIdleTimeSeconds: 600
    allIdleTimeSeconds: 600
    maxContentLength: 1048576
```

## RpcServerLauncher 的实现

```
package net.jkcode.jksoa.rpc.server

import net.jkcode.jkmvc.common.Config

/**
 * 服务器启动
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-16.
 */
object RpcServerLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // 获得服务端配置
        val config = Config.instance("rpc-server", "yaml")
        // 获得指定的协议的服务实例
        val protocol: String = config["protocol"]!!
        val server = IRpcServer.instance(protocol)
        // 启动服务
        server.start()
    }

}
```

默认的 protocol 是jkr, 其实我也可以这么调用, 一样的效果

```
JkrRpcServer().start()
```

## 注册server
server启动后的注册结果如下:

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        jkr:192.168.0.1:8080 # server节点, 格式是`协议:ip:端口`, 节点数据是参数, 如weight=1
        jkr:192.168.0.1:8080
    net.jkcode.jksoa.rpc.example.ISimpleService
        jkr:192.168.0.1:8080
        jkr:192.168.0.1:8080
```

其中对于`jkr:192.168.0.1:8080`, 即表示注册的server中ip是 192.168.0.1, 端口是8080, 协议是jkr