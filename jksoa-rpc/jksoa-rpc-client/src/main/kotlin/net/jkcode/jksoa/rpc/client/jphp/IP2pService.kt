package net.jkcode.jksoa.rpc.client.jphp

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
    fun callPhpFile(file: String, data: Map<String, Any?>): Any? {
        val lan = JphpLauncher
        val phpFile = Thread.currentThread().contextClassLoader.getResource("jphp/$file").path
        return lan.run(phpFile, data)
    }

    /**
     * 调用php方法
     * @param func php方法名, 如 User::sayHi()
     * @param params 方法参数
     * * @return
     */
    fun callPhpFunc(func: String, params: List<Any?>): Any? {
        val lan = JphpLauncher
        val phpFile = Thread.currentThread().contextClassLoader.getResource("jphp/callFunc.php").path
        val data = mapOf(
                "func" to func,
                "params" to params
        )
        return lan.run(phpFile, data)
    }

}