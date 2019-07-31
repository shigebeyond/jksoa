package net.jkcode.jksoa.rpc.loadbalance

import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection
import java.util.concurrent.ConcurrentHashMap

/**
 * ConsistentHash + 哈希码
 */
public typealias Hash2Code = Pair<ConsistentHash<IConnection>, Int>

/**
 * 一致性hash的均衡负载算法
 *
 * @author shijianhang
 * @create 2019-7-18 下午9:21
 **/
class ConsistentHashLoadBalancer : ILoadBalancer {

    /**
     * <服务标识, Hash2Code>
     */
    protected val hash2Codes = ConcurrentHashMap<String, Hash2Code>()

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

        // 一致性哈希
        return getConsistentHash(conns, req).get(req)
    }

    /**
     * 复用一致性哈希
     *
     * @param conn
     * @param req
     * @return
     */
    public fun getConsistentHash(conns: Collection<IConnection>, req: IRpcRequest): ConsistentHash<IConnection> {
        // 获得新连接的哈希码
        val code = System.identityHashCode(conns)

        // 获得旧连接的ConsistentHash + 哈希码
        val key = req.serviceId
        var hash2code = hash2Codes.get(key)

        // 对比旧连接的哈希码, 如果哈希码变了, 则代表连接变了
        if (hash2code == null || hash2code!!.second !== code) {
            val hash = ConsistentHash(3, 100, conns)
            hash2Codes.put(key, hash to code)
            hash2code = hash2Codes.get(key)
        }

        return hash2code!!.first
    }
}