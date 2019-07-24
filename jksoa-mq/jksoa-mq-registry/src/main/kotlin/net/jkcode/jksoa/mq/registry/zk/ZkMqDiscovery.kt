package net.jkcode.jksoa.mq.registry.zk

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.mq.common.exception.MqRegistryException
import net.jkcode.jksoa.mq.registry.*
import net.jkcode.jksoa.zk.ZkClientFactory
import org.I0Itec.zkclient.ZkClient

/**
 * 基于zookeeper的mq服务发现
 *    topic分配信息存zk, 本地不缓存
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
open class ZkMqDiscovery : IMqDiscovery {

    /**
     * 路径前缀
     */
    public val PathPrefix: String = "/jksoa-mq/"

    /**
     * topic分配所在路径
     */
    public val topic2brokerPath: String = PathPrefix + "topic2broker"

    /**
     * 注册中心配置
     */
    public val config: IConfig = Config.instance("registry", "yaml")

    /**
     * zk客户端
     */
    public val zkClient: ZkClient = ZkClientFactory.instance(config["zkConfigName"]!!)

    /**
     * zk节点数据监听器, 自发监听zk
     *
     * 对于 ZkMqDataListener
     *    继承了MqDiscoveryListenerContainer, 维护与代理多个 IMqDiscoveryListener
     *    实现了IZkDataListener, 处理zk数据变化
     */
    protected lateinit var dataListener: ZkMqDataListener

    init {
        // 创建topic分配的节点
        if (!zkClient.exists(topic2brokerPath))
            zkClient.createPersistent(topic2brokerPath, true)

        // 创建zk数据监听器
        dataListener = ZkMqDataListener(zkClient, topic2brokerPath)
        dataListener.start() // 开始监听

        // 刷新本地的topic分配信息, 通知监听器更新缓存的topic分配信息
        discover()
    }

    /**
     * 监听topic分配变化
     *
     * @param listener 监听器
     */
    public override fun subscribe(listener: IMqDiscoveryListener){
        try{
            clientLogger.info("ZkMqDiscovery监听topic分配变化")

            // 添加监听器
            this.dataListener.add(listener)
        } catch (e: Throwable) {
            throw MqRegistryException("ZkMqDiscovery监听topic分配变化失败：${e.message}", e)
        }
    }

    /**
     * 取消topic分配变化
     *
     * @param listener 监听器
     */
    public override fun unsubscribe(listener: IMqDiscoveryListener){
        try{
            // 删除监听器
            dataListener.remove(listener)
            if(dataListener.isEmpty())
                dataListener.close()
        } catch (e: Throwable) {
            throw MqRegistryException("ZkMqDiscovery取消监听topic分配变化失败：${e.message}", e)
        }
    }

    /**
     * 发现topic分配
     *
     * @return <topic, broker>
     */
    public override fun discover(): TopicAssignment {
        try {
            // 获得节点数据
            val json = zkClient.readData(topic2brokerPath) as String?
            val assignment = if(json == null) HashMap() else json2TopicAssignment(json)

            // 主动触发本地监听器
            dataListener.handleTopic2BrokerChange(assignment)

            return assignment
        } catch (e: Throwable) {
            throw MqRegistryException("发现topic分配失败：${e.message}", e)
        }

    }

}