# 负载均衡

LoadBalance 为负载均衡，它的职责是将网络请求，或者其他形式的负载“均摊”到不同的机器上。避免集群中部分服务器压力过大，而另一些服务器比较空闲的情况。通过负载均衡，可以让每台服务器获取到适合自己处理能力的负载。在为高负载服务器分流的同时，还可以避免资源浪费，一举两得。

负载均衡可分为软件负载均衡和硬件负载均衡。软件负载均衡如 Nginx。

jksoa-rpc 中负载均衡主要在client端给服务提供者发送请求时触发, 均衡这些服务提供者的负载. 否则某个服务提供者的负载过大，会导致部分请求超时。

jksoa-rpc 提供了3种负载均衡实现, 缺省为 random 随机调用.
1. 基于权重随机算法的 `RandomLoadBalancer`
2. 基于 hash 一致性的 `ConsistentHashLoadBalancer`
3. 基于加权轮询算法的 `RoundRobinLoadBalancer`。

## 类族

负载策略的接口是 `ILoadBalancer`

```
package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection

/**
 * 选择连接的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:04
 **/
interface ILoadBalancer {

    /**
     * 选择连接
     *
     * @param conns
     * @param req
     * @return
     */
    fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection?
}
```

类族为

```
ILoadBalancer
    RandomLoadBalancer -- 随机
    ConsistentHashLoadBalancer -- 一致性hash
    RoundRobinLoadBalancer -- 轮询
```

接下来逐个负载策略来说明

## RandomLoadBalancer
随机，按权重设置随机概率。
在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。

## ConsistentHashLoadBalancer
一致性 Hash，相同参数的请求总是发到同一提供者。

当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。

算法参见：http://en.wikipedia.org/wiki/Consistent_hashing

## RoundRobinLoadBalancer
轮询，按公约后的权重设置轮询比率。
存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。

# 配置负载策略

目前我只在客户端发送请求时, 使用负载策略, 因此配置只针对客户端

## 客户端级别配置

在配置文件`rpc-client.yaml`中的属性`loadbalancer` 来指定

```
loadbalancer: random # 均衡负载类型
```

## 方法级别配置

在注解 `@RemoteService` 的属性 `loadBalancer` 来指定

```
@RemoteService(loadBalancer = "consistentHash")
interface IMyService
```

# 扩展负载策略

## 1. 实现接口`ILoadBalancer`

参考 `RandomLoadBalancer`

```
package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection

/**
 * 随机的均衡负载算法
 *
 * @author shijianhang
 * @create 2017-12-18 下午9:21
 **/
class RandomLoadBalancer : ILoadBalancer {
    /**
     * 选择连接
     */
    public override fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection? {
        if(conns.isEmpty())
            return null

        // 有权重的集合
        val col = WeightCollection(conns)

        // 随机选个连接
        val i = randomInt(col.size)
        //println("select: $i from: 0 util ${col.size}")
        return col.get(i)
    }
}
```

## 2. 在`load-balancer.yaml`配置策略名+实现类

```
# 均衡负载类型
random: net.jkcode.jksoa.rpc.loadbalance.RandomLoadBalancer # 随机选择
consistentHash: net.jkcode.jksoa.rpc.loadbalance.ConsistentHashLoadBalancer # 一致性hash
roundRobin: net.jkcode.jksoa.rpc.loadbalance.RoundRobinLoadBalancer # 轮询
```

## 3. 通过策略名来引用策略

```
val loadBalancer: ILoadBalancer = ILoadBalancer.instance("策略名")
val conn = loadBalancer.select(conns)
```