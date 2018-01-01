package com.jksoa.registry.zk.listener

import com.jksoa.common.Url
import com.jksoa.common.registerLogger
import com.jksoa.registry.IDiscoveryListener
import com.jksoa.registry.zk.common.ZkClientFactory
import com.jksoa.registry.zk.common.nodeChilds2Urls
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient

/**
 * zk中子节点变化监听器
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:56
 **/
class ZkChildListener(public val discoveryListener: IDiscoveryListener): IZkChildListener {

    /**
     * zk客户端
     */
    public val zkClient: ZkClient = ZkClientFactory.instance()

    /**
     * 处理zk中子节点变化事件
     *
     * @param parentPath
     * @param currentChilds
     */
    @Synchronized
    public override fun handleChildChange(parentPath: String, currentChilds: List<String>) {
        try {
            // 更新服务地址
            val serviceId = Url.rootPath2serviceId(parentPath)
            discoveryListener.handleServiceUrlsChange(serviceId, zkClient.nodeChilds2Urls(parentPath, currentChilds))
            registerLogger.info("处理zk[$parentPath]子节点变化事件, 子节点为: $currentChilds")
        }catch(e: Exception){
            registerLogger.error("处理zk[$parentPath]子节点变化事件失败", e)
            throw e
        }
    }


}