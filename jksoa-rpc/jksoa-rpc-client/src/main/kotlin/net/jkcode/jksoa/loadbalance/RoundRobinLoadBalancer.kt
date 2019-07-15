package net.jkcode.jksoa.loadbalance

import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.client.IConnection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 轮询的均衡负载算法
 *
 * @author shijianhang
 * @create 2019-7-18 下午9:21
 **/
class RoundRobinLoadBalancer : ILoadBalancer {

    /**
     * 缓存计数器 <serviceId, 计数器>
     */
    protected val counters: ConcurrentHashMap<String, AtomicInteger> = ConcurrentHashMap();

    /**
     * 选择连接
     *    TODO: 添加权重因子 IConnection.weight
     *
     * @param conn
     * @param req
     * @return
     */
    public override fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection? {
        if(conns.isEmpty())
            return null

        val counter = counters.getOrPut(req.serviceId){
            AtomicInteger(0)
        }
        val i = counter.getAndIncrement() % conns.size
        return conns[i]
    }
}