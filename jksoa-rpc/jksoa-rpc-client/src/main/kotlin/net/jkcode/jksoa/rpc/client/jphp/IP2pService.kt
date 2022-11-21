package net.jkcode.jksoa.rpc.client.jphp

import java.util.concurrent.CompletableFuture
import net.jkcode.jphp.ext.JphpLauncher

/**
 * 代理调用php服务
 */
interface IP2pService {

    /**
     * 调用php文件
     * @param file php文件名
     * @param data 参数
     * * @return
     */
    fun callPhpFile(file: String, data: Map<String, Any?>): CompletableFuture<Any?> {
        val lan = JphpLauncher
        val phpFile = Thread.currentThread().contextClassLoader.getResource("jphp/$file").path
        // JphpLauncher.run()中php执行结果有可能是WrapCompletableFuture, 他直接返回future, 以便调用端处理异步结果
        val ret = lan.run(phpFile, data)
        if(ret is CompletableFuture<*>)
            return ret as CompletableFuture<Any?>
        return CompletableFuture.completedFuture(ret)
    }

    /**
     * 调用php方法
     * @param func php方法名, 如 User::sayHi()
     * @param params 方法参数
     * * @return
     */
    fun callPhpFunc(func: String, params: List<Any?>): CompletableFuture<Any?> {
        val data = mapOf(
            "func" to func,
            "params" to params
        )
        return callPhpFile("jphp/callFunc.php", data)
    }

}