# 概述

目前josoa-rpc支持有2种通讯协议:

1. rmi协议

2. netty实现的协议, 默认使用这种协议

因此导致server也有两种实现

1. RmiServer, 实现rmi协议

2. NettyServer, 实现netty协议

其类族如下

```
IRpcServer
	NettyServer
	RmiServer
```

# 启动server

使用 `net.jkcode.jksoa.server.RpcServerLauncher` 作为主类, 其`main()`方法会启动server

实际上他的实现很简单, 就是根据 `server.yaml` 配置文件中指定的 protocol 协议去调用对应的 IRpcServer 的实现类

```
package net.jkcode.jksoa.server

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
        val config = Config.instance("server", "yaml")
        // 获得指定的协议的服务实例
        val protocol: String = config["protocol"]!!
        val server = IRpcServer.instance(protocol)
        // 启动服务
        server.start()
    }

}
```

默认的 protocol 是netty, 其实我也可以这么调用, 一样的效果

```
NettyServer().start()
```