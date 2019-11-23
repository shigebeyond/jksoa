package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkutil.common.get
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection
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
     *
     * @param conn
     * @param req
     * @return
     */
    public override fun select(conns: Collection<IConnection>, req: IRpcRequest): IConnection? {
        if(conns.isEmpty())
            return null

        // 有权重的集合
        val col = WeightCollection(conns)

        // 轮询
        val counter = counters.getOrPut(req.serviceId){
            AtomicInteger(0)
        }
        val i = counter.getAndIncrement() % col.size
        //println("select: $i from: 0 util ${col.size}")
        return col.get(i)
    }
}