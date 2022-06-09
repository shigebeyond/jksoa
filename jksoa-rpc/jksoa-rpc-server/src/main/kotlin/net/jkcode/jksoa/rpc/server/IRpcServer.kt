package net.jkcode.jksoa.rpc.server

import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.rpc.server.provider.ProviderLoader
import net.jkcode.jkutil.common.*
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
        public override val instsConfig: IConfig = Config.instance("rpc-protocol.server", "yaml")

        /**
         * 服务端配置
         */
        public val config = Config.instance("rpc-server", "yaml")

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
     * 服务器url
     */
    public val serverUrl: Url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!)

    /**
     * 服务器名
     */
    public val name: String
        get(){
            val clazz = this.javaClass.name
            val i = clazz.lastIndexOf('.')
            return clazz.substring(i + 1)
        }

    /**
     * 启动服务器
     * @param waitingClose 是否等待关闭
     * @param callback 启动后回调
     */
    public fun start(waitingClose: Boolean = true, callback: (() -> Unit)? = null){
        // 启动服务器
        try{
            serverLogger.debug(" ------ start rpc server ------ ")
            serverLogger.info("{}在地址[{}]上启动", name, serverUrl)
            server = this
            // 可能阻塞，只能在最后一句执行
            doStart(waitingClose) {
                //启动后，主动调用 ProviderLoader.load() 来扫描加载Provider服务
                ProviderLoader.load()

                // 初始化插件
                PluginLoader.loadPlugins()

                // 调用回调
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
     * @param waitingClose 是否等待关闭
     * @param callback 启动后回调
     */
    public abstract fun doStart(waitingClose: Boolean, callback: () -> Unit)

    /**
     * 关闭server
     */
    public override fun close(){
        server = null
    }
}