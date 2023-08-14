package net.jkcode.jksoa.rpc.tests

import co.paralleluniverse.fibers.FiberExecutorScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import com.alibaba.fastjson.JSON
import io.netty.channel.DefaultEventLoop
import net.jkcode.jkutil.common.*
import net.jkcode.jkutil.serialize.FstSerializer
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.rpc.client.k8s.router.IRpcRouter
import net.jkcode.jksoa.rpc.client.protocol.jkr.JkrRpcClient
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.client.k8s.router.PatternRpcRouter
import net.jkcode.jksoa.rpc.example.IGuardService
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.common.getRootResource
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.jvm.javaMethod


/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2023-7-14 3:11 PM
 */
class RpcRouterTests {

    @Test
    fun testResovleServer() {
        var req = RpcRequest(ISimpleService::sayHi, arrayOf<Any?>("shi"))
        var server = PatternRpcRouter.resovleServer(req)
        println("[" + req + "]'s server = " + server)
        req.putAttachment(IRpcRouter.ROUTE_TAG_NAME, "test")
        server = PatternRpcRouter.resovleServer(req)
        println("[" + req + "]'s server = " + server)
    }


}