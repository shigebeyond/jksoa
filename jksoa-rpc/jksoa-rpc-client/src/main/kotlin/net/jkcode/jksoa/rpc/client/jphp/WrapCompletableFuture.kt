package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jphp.ext.callMemoryOrAny
import php.runtime.Memory
import php.runtime.annotation.Reflection
import php.runtime.env.Environment
import php.runtime.invoke.Invoker
import php.runtime.lang.BaseObject
import php.runtime.memory.support.MemoryUtils
import php.runtime.reflection.ClassEntity
import java.util.concurrent.*

@Reflection.Name("CompletableFuture")
@Reflection.Namespace(JksoaRpcExtension.NS)
class WrapCompletableFuture(env: Environment, protected val future: CompletableFuture<*>) : BaseObject(env) {

    // ---------------------- Future 方法扩展: 参考 WrapFuture ----------------------
    @Reflection.Signature
    private fun __construct(env: Environment, vararg args: Memory): Memory {
        return Memory.NULL
    }

    @Reflection.Signature
    fun isCancelled(env: Environment, vararg args: Memory): Memory {
        return if (future.isCancelled) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature
    fun isDone(env: Environment, vararg args: Memory): Memory {
        return if (future.isDone) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature(Reflection.Arg("mayInterruptIfRunning"))
    fun cancel(env: Environment, vararg args: Memory): Memory {
        return if (future.cancel(args[0].toBoolean())) Memory.TRUE else Memory.FALSE
    }

    @Reflection.Signature(Reflection.Arg(value = "timeout", optional = Reflection.Optional("NULL")))
    fun get(env: Environment, vararg args: Memory): Memory {
        val v = if (args[0].isNull) future.get() else future[args[0].toLong(), TimeUnit.MILLISECONDS]
        if(v == null)
            return Memory.NULL
        return MemoryUtils.valueOf(env, v)
    }

    // ---------------------- CompletableFuture 方法扩展 ----------------------
    @Reflection.Signature
    fun thenAccept(env: Environment, invoker: Invoker): WrapCompletableFuture {
        val f = future.thenAccept { v ->
            invoker.callMemoryOrAny(v)
        }
        return WrapCompletableFuture(env, f)
    }

    @Reflection.Signature
    fun thenApply(env: Environment, invoker: Invoker): WrapCompletableFuture {
        val f = future.thenApply { v ->
            invoker.callMemoryOrAny(v)
        }
        return WrapCompletableFuture(env, f)
    }

    @Reflection.Signature
    fun exceptionally(env: Environment, invoker: Invoker): WrapCompletableFuture {
        val f = future.exceptionally { ex ->
            invoker.callMemoryOrAny(ex) as Nothing?
        }
        return WrapCompletableFuture(env, f)
    }

    @Reflection.Signature
    fun whenComplete(env: Environment, invoker: Invoker): WrapCompletableFuture {
        val f = future.whenComplete { r, ex ->
            invoker.callMemoryOrAny(r, ex)
        }
        return WrapCompletableFuture(env, f)
    }

}
