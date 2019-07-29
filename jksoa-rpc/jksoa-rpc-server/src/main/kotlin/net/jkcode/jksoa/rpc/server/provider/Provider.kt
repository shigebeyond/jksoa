package net.jkcode.jksoa.rpc.server.provider

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.singleton.BeanSingletons
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.leader.ZkLeaderElection
import net.jkcode.jksoa.rpc.registry.IRegistry
import net.jkcode.jksoa.rpc.registry.zk.ZkRegistry
import net.jkcode.jksoa.rpc.server.IProvider
import net.jkcode.jksoa.rpc.server.IRpcServer

/**
 * 服务提供者
 *   1 提供服务
 *   2 向注册中心注册服务
 *   3 在启动server时调用
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

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = ZkRegistry
    }

    /**
     * 是否注册 = 是否启动了server
     *    只有启动了server, 暴露了server端口才能注册
     *    如果是纯粹的client(如mq consumer提供IMqPushConsumerService服务), 但不能向注册中心注册, 也因为他没有暴露server端口, 无法让client来连接并调用
     */
    public val registerable: Boolean = IRpcServer.current() != null

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
        Url(serverUrl.protocol, serverUrl.host, serverUrl.port, `interface`.name, config.getMap("parameters", emptyMap<String, Any?>())!!);
    }

    /**
     * 服务实例
     *   延迟实例化, 如果注解onlyLeader为true, 则必须当选leader才实例化
     */
    public override lateinit var service: Any

    init{
        // 创建+注册服务
        if(`interface`.remoteService?.onlyLeader ?: false){ // 要选举leader
            // 先选举leader才创建+注册服务
            val election = ZkLeaderElection(serviceId)
            election.run(){
                createAndRegisterService()
            }
        }else // 直接创建+注册服务
            createAndRegisterService()
    }

    /**
     * 创建+注册服务
     */
    protected fun createAndRegisterService() {
        // 创建服务实例
        service = BeanSingletons.instance(clazz)

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
        if(registerable){
            serverLogger.info("Provider.close(): 注销服务")
            registry.unregister(serviceUrl)
        }
    }

}