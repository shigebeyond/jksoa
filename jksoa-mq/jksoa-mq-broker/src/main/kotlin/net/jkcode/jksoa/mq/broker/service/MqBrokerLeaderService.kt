package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.mq.common.GroupSequence
import net.jkcode.jksoa.mq.common.TopicSequence
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.rpc.registry.IDiscoveryListener
import net.jkcode.jksoa.rpc.registry.IRegistry

/**
 * 消息中转者的leader服务
 *    1. 监听broker服务节点变化, 以便重新分配topic
 *    2. 注册/注销topic
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerLeaderService : IMqBrokerLeaderService, IDiscoveryListener {

    /**
     * rpc的注册中心
     */
    public val rpcRegistry: IRegistry = IRegistry.instance("zk")

    /**
     * 注册中心
     */
    protected val mqRegistry: IMqRegistry = IMqRegistry.instance("zk")

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
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        // 注销broker: 将该broker上的topic重新分配给其他broker
        mqRegistry.unregisterBroker(url, allUrls)
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     * @param url
     */
    public override fun handleParametersChange(url: Url) {
        // do thing
    }

    /********************* 注册/注销topic **********************/
    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    public override fun registerTopic(topic: String): Boolean {
        // 初始化主题id
        TopicSequence.getOrCreate(topic)

        // 注册主题
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

    /**
     * 注册分组
     * @param group 分组
     */
    public override fun registerGroup(group: String){
        // 初始化分组id
        GroupSequence.getOrCreate(group)
    }

}