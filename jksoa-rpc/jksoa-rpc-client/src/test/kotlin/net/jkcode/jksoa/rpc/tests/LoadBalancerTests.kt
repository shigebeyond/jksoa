package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkutil.common.randomInt
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jksoa.rpc.loadbalance.ILoadBalancer
import net.jkcode.jksoa.rpc.loadbalance.WeightCollection
import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-31 8:15 PM
 */
class LoadBalancerTests {

    val conns = (0..2).map {
        TestConnection(randomInt(5))
    }

    val req = RpcRequest(ISimpleService::sayHi)

    init {
        println("有连接: " + conns)
    }

    @Test
    fun testWeightCollection(){
        // 有权重的集合
        val col = WeightCollection(conns)
        for(item in col)
            println(item)
    }

    fun testLoadBalancer(name: String){
        val loadBalancer: ILoadBalancer = ILoadBalancer.instance(name)
        for(i in 0..5) {
            val conn = loadBalancer.select(conns, req)
            println(conn)
        }
    }

    @Test
    fun testRandom(){
        testLoadBalancer("random")
    }

    @Test
    fun testRoundRobin(){
        testLoadBalancer("roundRobin")
    }

}