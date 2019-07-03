package net.jkcode.jksoa.server

import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jkmvc.common.getIntranetHost
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.provider.ProviderLoader
import java.io.Closeable

/**
 * rpc协议-服务器端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 **/
abstract class IRpcServer: Closeable {

    // 可配置的单例
    companion object mxx: NamedConfiguredSingletons<IRpcServer>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("protocol.server", "yaml")

        /**
         * 服务端配置
         */
        public val config = Config.instance("server", "yaml")

        /**
         * 当前启动的服务器
         */
        protected var server: IRpcServer? = null

        /**
         * 获得当前启动的服务器
         *   可借此来判断是否启动了服务器
         */
        @JvmStatic
        public fun current(): IRpcServer? {
            return server
        }
    }

    /**
     * 插件列表
     */
    public val plugins: List<IPlugin> = config.classes2Instances("plugins")

    /**
     * 服务器url
     */
    val serverUrl: Url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!)

    /**
     * 服务器名
     */
    val name: String
        get(){
            val clazz = this.javaClass.name
            val i = clazz.lastIndexOf('.')
            return clazz.substring(i + 1)
        }

    /**
     * 启动服务器
     * @param callback 启动后回调
     */
    fun start(callback: (() -> Unit)? = null){
        // 启动服务器
        try{
            serverLogger.info("{}在地址[{}]上启动", name, serverUrl)
            server = this
            // 可能阻塞，只能在最后一句执行
            doStart() {
                //启动后，主动调用 ProviderLoader.load() 来扫描加载Provider服务
                ProviderLoader.load()

                // 初始化插件
                for(p in plugins)
                    p.start()

                // 调用原来的回调
                callback?.invoke()

                // 关机时要关闭
                ClosingOnShutdown.addClosing(this)
            }
        }catch(e: Exception){
            serverLogger.error("${name}在地址[$serverUrl]上启动失败", e)
            throw RpcServerException(e)
        }
    }

    /**
     * 启动服务器
     * @param callback 启动后回调
     */
    abstract fun doStart(callback: () -> Unit)

    /**
     * 关闭server
     */
    override fun close(){
        // 关闭插件
        for(p in plugins)
            p.close()
    }
}