package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import net.jkcode.jksoa.registry.IDiscoveryListener
import net.jkcode.jksoa.registry.IRegistry
import net.jkcode.jksoa.registry.zk.ZkRegistry

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerLeaderService : IMqBrokerLeaderService, IDiscoveryListener {

    /**
     * rpc的注册中心
     */
    public val rpcRegistry: IRegistry = ZkRegistry

    /**
     * 注册中心
     */
    protected val mqRegistry: IMqRegistry = ZkMqRegistry

    /********************* 监听broker服务节点变化 **********************/
    /**
     * 服务标识，即接口类全名
     */
    public override val serviceId: String = IMqBrokerService::class.qualifiedName!!

    init {
        // 监听broker服务节点变化, 以便重新分配topic
        rpcRegistry.subscribe(serviceId, this)
    }

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        // do thing
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(serverName: String, allUrls: Collection<Url>) {
        // 注销broker: 将该broker上的topic重新分配给其他broker
        mqRegistry.unregisterBroker(serverName, allUrls)
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     * @param url
     * @param allUrls
     */
    public override fun handleParametersChange(url: Url, allUrls: Collection<Url>) {
        // do thing
    }

    /********************* 注册topic分配情况 **********************/
    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    public override fun registerTopic(topic: String): Boolean {
        return mqRegistry.registerTopic(topic)
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    public override fun unregisterTopic(topic: String): Boolean {
        return mqRegistry.unregisterTopic(topic)
    }

}