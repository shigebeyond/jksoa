package com.jksoa.registry.zk.listener

import com.jksoa.common.Url
import com.jksoa.registry.IDiscoveryListener
import com.jksoa.registry.registerLogger

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
        // 处理更新地址
        url.parameters = Url.parseParams(data as String)
        discoveryListener.handleParametersChange(url)
        registerLogger.info("[ZookeeperRegistry] command data change: path=%s, command=%s", dataPath, data)
    }

    /**
     * 处理zk中节点数据删除事件
     */
    @Synchronized
    public override fun handleDataDeleted(dataPath: String) {
        // 处理更新地址
        url.parameters = null
        discoveryListener.handleParametersChange(url)
        registerLogger.info("[ZookeeperRegistry] command deleted: path=%s", dataPath)
    }
}