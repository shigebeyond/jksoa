package net.jkcode.jksoa.rpc.client.jphp

import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.lang.BaseObject
import php.runtime.reflection.ClassEntity
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Reflection.Name("Future")
@Reflection.Namespace(JksoaRpcExtension.NS)
class WrapFuture(env: Environment, protected val future: Future<Memory>) : BaseObject(env) {

    @Reflection.Signature
    private fun __construct(env: Environment, vararg args: Memory): Memory {
        return Memory.NULL
    }

    @Reflection.Signature
    fun isCancelled(env: Environment, vararg args: Memory?): Memory {
        return if (future.isCancelled) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature
    fun isDone(env: Environment, vararg args: Memory?): Memory {
        return if (future.isDone) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature(Reflection.Arg("mayInterruptIfRunning"))
    fun cancel(env: Environment, vararg args: Memory): Memory {
        return if (future.cancel(args[0].toBoolean())) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature(Reflection.Arg(value = "timeout", optional = Reflection.Optional("NULL")))
    @Throws(ExecutionException::class, InterruptedException::class, TimeoutException::class)
    operator fun get(env: Environment, vararg args: Memory): Memory {
        return if (args[0].isNull) future.get() else future[args[0].toLong(), TimeUnit.MILLISECONDS]
    }
}