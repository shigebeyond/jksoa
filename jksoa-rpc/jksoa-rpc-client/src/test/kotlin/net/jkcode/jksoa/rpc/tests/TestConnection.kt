package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import java.lang.UnsupportedOperationException
import java.util.concurrent.atomic.AtomicInteger

/**
 * 测试用的连接
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-31 8:18 PM
 */
class TestConnection(weight: Int): BaseConnection(Url("http://jkcode.net"), weight) {

    companion object{
        // 计数, 用于生成id
        val counter = AtomicInteger()
    }

    protected val id = counter.getAndIncrement()

    override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        throw UnsupportedOperationException()
    }

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun toString(): String {
        return "TestConnection(id=$id, weight=$weight)"
    }
}