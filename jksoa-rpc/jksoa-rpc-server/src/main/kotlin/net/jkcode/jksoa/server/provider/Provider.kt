package net.jkcode.jksoa.server.provider

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.singleton.BeanSingletons
import net.jkcode.jksoa.client.referer.RefererLoader
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.leader.ZkLeaderElection
import net.jkcode.jksoa.registry.IRegistry
import net.jkcode.jksoa.registry.zk.ZkRegistry
import net.jkcode.jksoa.server.IProvider
import net.jkcode.jksoa.server.IRpcServer

/**
 * 服务提供者
 *   1 提供服务
 *   2 向注册中心注册服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
class Provider(public override val clazz: Class<*> /* 实现类 */, public val registerable: Boolean /* 是否注册 */) : IProvider() {

    companion object{
        /**
         * 服务端配置
         */
        public val config = Config.instance("server", "yaml")

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = ZkRegistry
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
     * 服务路径
     */
    public override val serviceUrl:Url by lazy{
        // ip端口
        val serverUrl = IRpcServer.current()!!.serverUrl
        // 协议: 虽然server也有定义协议, 但是以service中定义的为准
        val protocol = `interface`.remoteService!!.protocol
        Url(protocol, serverUrl.host, serverUrl.port, `interface`.name, config.getMap("parameters", emptyMap<String, Any?>())!!);
    }

    /**
     * 服务实例
     */
    public override val service: Any = BeanSingletons.instance(clazz)

    init{
        if(`interface`.remoteService?.onlyLeader ?: false){ // 要选举leader
            // 先选举leader才注册服务
            val election = ZkLeaderElection(serviceId)
            election.run(){
                registerService()
            }
        }else // 直接注册服务
            registerService()
    }

    /**
     * 注册服务
     */
    protected fun registerService() {
        if (registerable) {
            serverLogger.info("Provider注册服务: {}", serviceUrl)
            // 1 注册注册中心的服务
            registry.register(serviceUrl)

            // 2 注册本地服务引用： 对要调用的服务，如果本地有提供，则直接调用本地的服务
            RefererLoader.addLocal(`interface`, service)
        }
    }

    /**
     * 注销服务
     */
    public override fun close() {
        serverLogger.info("Provider.close(): 注销服务")
        registry.unregister(serviceUrl)
    }

}