package com.jksoa.registry.zk.listener

import com.jksoa.common.Url
import com.jksoa.common.registerLogger
import com.jksoa.registry.IDiscoveryListener

/**
 * zk中节点数据变化监听器
 *
 * @author shijianhang
 * @create 2017-12-14 上午12:25
 **/
class ZkDataListener(public val url: com.jksoa.common.Url, public val discoveryListener: IDiscoveryListener): org.I0Itec.zkclient.IZkDataListener {

    /**
     * 处理zk中节点数据变化事件
     */
    @Synchronized
    public override fun handleDataChange(dataPath: String, data: Any) {
        try {
            // 处理更新地址
            url.parameters = Url.parseParams(data as String)
            discoveryListener.handleParametersChange(url)
            registerLogger.info("[ZookeeperRegistry] command data change: path=%s, command=%s", dataPath, data)
        }catch(e: Exception){
            registerLogger.error("处理zk节点数据变化事件失败", e)
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
            url.parameters = null
            discoveryListener.handleParametersChange(url)
            registerLogger.info("[ZookeeperRegistry] command deleted: path=%s", dataPath)
        }catch(e: Exception){
            registerLogger.error("处理zk节点数据删除事件失败", e)
            throw e
        }
    }
}