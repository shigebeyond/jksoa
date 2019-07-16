package net.jkcode.jksoa.mq.registry.zk

import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.json2TopicAssignment
import org.I0Itec.zkclient.IZkDataListener

/**
 * zk中节点数据变化监听器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 **/
class ZkMqDataListener(public val discoveryListener: IMqDiscoveryListener): IZkDataListener {

    /**
     * 处理zk中节点数据变化事件
     */
    @Synchronized
    public override fun handleDataChange(dataPath: String, data: Any) {
        try {
            // 处理topic分配更新
            val assign = json2TopicAssignment(data as String)
            discoveryListener.handleTopic2BrokerChange(assign)
            registerLogger.info("处理zk节点[{}]数据变化事件，数据为: {}", dataPath, data)
        }catch(e: Exception){
            registerLogger.error("处理zk节点[$dataPath]数据变化事件失败", e)
            throw e
        }
    }

    /**
     * 处理zk中节点数据删除事件
     */
    @Synchronized
    public override fun handleDataDeleted(dataPath: String) {
        // TODO
    }
}