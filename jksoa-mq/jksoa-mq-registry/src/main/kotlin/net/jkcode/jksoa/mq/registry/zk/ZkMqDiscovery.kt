package net.jkcode.jksoa.mq.registry.zk

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.mq.registry.IMqDiscovery
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.json2TopicAssignment
import net.jkcode.jksoa.registry.RegistryException
import net.jkcode.jksoa.zk.ZkClientFactory
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于zookeeper的mq服务发现
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
     * zk节点数据监听器
     */
    protected val dataListeners = ConcurrentHashMap<IMqDiscoveryListener, ZkMqDataListener>()

    /**
     * 监听topic分配变化
     *
     * @param listener 监听器
     */
    override fun subscribe(listener: IMqDiscoveryListener){
        try{
            clientLogger.info("ZkMqDiscovery监听topic分配变化")

            // 监听节点的数据变化
            val dataListener = ZkMqDataListener(listener)
            zkClient.subscribeDataChanges(topic2brokerPath, dataListener);

            // 记录监听器，以便取消监听时使用
            dataListeners.put(listener, dataListener)
        } catch (e: Throwable) {
            throw RegistryException("ZkMqDiscovery监听topic分配变化失败：${e.message}", e)
        }
    }

    /**
     * 取消topic分配变化
     *
     * @param listener 监听器
     */
    override fun unsubscribe(listener: IMqDiscoveryListener){
        try{
            // 取消监听节点的数据变化
            val dataListener = dataListeners.get(listener)
            zkClient.unsubscribeDataChanges(topic2brokerPath, dataListener)
        } catch (e: Throwable) {
            throw RegistryException("ZkMqDiscovery取消监听topic分配变化失败：${e.message}", e)
        }
    }

    /**
     * 发现topic分配
     *
     * @return 服务地址
     */
    override fun discover(): TopicAssignment {
        try {
            // 获得节点数据
            val json = zkClient.readData(topic2brokerPath) as String
            return json2TopicAssignment(json)
        } catch (e: Throwable) {
            throw RegistryException("发现topic分配失败：${e.message}", e)
        }

    }

}