package com.jksoa.registry.zk

import com.jksoa.common.INotifyListener
import com.jksoa.common.Url
import com.jksoa.registry.registerLogger
import org.I0Itec.zkclient.IZkDataListener

/**
 * zk中节点数据变化监听器
 *
 * @author shijianhang
 * @create 2017-12-14 上午12:25
 **/
class ZkDataListener(public val url: Url, public val notifyListener: INotifyListener): IZkDataListener {

    /**
     * 处理zk中节点数据变化事件
     */
    @Synchronized
    public override fun handleDataChange(dataPath: String, data: Any) {
        // 处理更新地址
        url.parameters = Url.parseParams(data as String)
        notifyListener.handleUpdateUrl(url)
        registerLogger.info("[ZookeeperRegistry] command data change: path=%s, command=%s", dataPath, data)
    }

    /**
     * 处理zk中节点数据删除事件
     */
    @Synchronized
    public override fun handleDataDeleted(dataPath: String) {
        // 处理更新地址
        url.parameters = null
        notifyListener.handleUpdateUrl(url)
        registerLogger.info("[ZookeeperRegistry] command deleted: path=%s", dataPath)
    }
}