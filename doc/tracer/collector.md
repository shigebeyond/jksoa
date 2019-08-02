# collector模块

没啥好说, 就是提供远端服务 `ICollectorService` 来收集agent上传过来的跟踪数据

## ICollectorService 接口

根据跟踪数据的存储方式不同, 有不同的实现, 目前只支持mysql存储

```
package net.jkcode.jksoa.tracer.common.service.remote

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

/**
 * collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
@RemoteService(version = 1)
interface ICollectorService {

    /**
     * 同步服务
     *    1 只新增, 不删除
     *    2 返回所有应用的service, 用于获得service对应的id, 给span引用, 存储时省点空间
     *
     * @param appName 应用名
     * @param serviceName 服务全类名
     * @return 所有应用的service的name对id的映射
     */
    fun syncServices(appName: String, serviceName: List<String>): Map<String, Int>

    /**
     * agent给collector发送span
     *
     * @param spans
     * @return
     */
    fun send(spans: List<Span>): CompletableFuture<Unit>

}
```

## TODO

1. 跟踪数据的存储方式, 支持存到hbase等高性能高容量的存储中

2. 同步服务, 无非就是给服务生成id, 可以不用依赖mysql, 用我在mq中的组件`ZkSequence`, 但注意agent要提前同步, 因为`ZkSequence`是基于zookeeper来给其他参与者分发服务与id, 是有延迟的, 当参与方没有收到就调用他就会出错