package net.jkcode.jksoa.rpc.server.provider

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.singleton.BeanSingletons
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jksoa.rpc.server.IProvider
import net.jkcode.jksoa.rpc.server.IRpcServer

/**
 * 服务提供者
 *   1 提供服务
 *   2 在启动server时调用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
class Provider(public override val clazz: Class<*> /* 实现类 */) : IProvider() {

    companion object{
        /**
         * 服务端配置
         */
        public val config = Config.instance("rpc-server", "yaml")
    }

    /**
     * 接口类
     */
    public override val `interface`: Class<*> by lazy{
        // 遍历接口
        clazz.interfaces.first {
            it.remoteService != null // 过滤 @RemoteService 注解
        }
    }

    /**
     * 服务路径： 无用
     */
    public override val serviceUrl:Url by lazy{
        // ip端口
        val serverUrl = IRpcServer.current()!!.serverUrl
        Url(serverUrl.protocol, serverUrl.host, serverUrl.port, `interface`.name, config.getMap("parameters", emptyMap<String, Any?>())!!);
    }

    /**
     * 创建服务实例
     */
    public override val service: Any = BeanSingletons.instance(clazz)

    init {
        // 注册本地服务引用： 对要调用的服务，如果本地有提供，则直接调用本地的服务
        RefererLoader.addLocal(`interface`, service)
    }

    /**
     * 注销服务
     */
    public override fun close() {
    }

}