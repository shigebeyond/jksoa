package net.jkcode.jksoa.rpc.registry.zk.listener

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.rpc.registry.IDiscoveryListener
import org.I0Itec.zkclient.IZkDataListener

/**
 * zk中节点数据变化监听器
 *
 * @author shijianhang
 * @create 2017-12-14 上午12:25
 **/
class ZkDataListener(public val url: Url, public val discoveryListener: IDiscoveryListener): IZkDataListener {

    /**
     * 处理zk中节点数据变化事件
     */
    @Synchronized
    public override fun handleDataChange(dataPath: String, data: Any) {
        try {
            // 处理更新地址
            url.parameters = Url.parseParams(data as String)
            discoveryListener.handleParametersChange(url)
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
        try {
            // 处理更新地址
            url.parameters = emptyMap()
            discoveryListener.handleParametersChange(url)
            registerLogger.info("处理zk节点[{}]数据删除事件", dataPath)
        }catch(e: Exception){
            registerLogger.error("处理zk节点[$dataPath]数据删除事件失败", e)
            throw e
        }
    }
}