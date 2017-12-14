package com.jksoa.registry.zk

import com.jksoa.common.INotifyListener
import com.jksoa.common.Url
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by shi on 12/14/17.
 */
open class ZkDiscovery {
    /**
     * zk客户端
     */
    protected val zkClient: ZkClient = ZkClientFactory.instance()
    /**
     * zk子节点监听器: <服务名 to <服务监听器 to zk监听器>>
     */
    private val childListeners = ConcurrentHashMap<String, ConcurrentHashMap<INotifyListener, ZkChildListener>>()
    /**
     * zk节点数据监听器: <服务名 to <服务监听器 to zk监听器>>
     */
    private val dataListeners = ConcurrentHashMap<String, ConcurrentHashMap<INotifyListener, List<ZkDataListener>>>()

    /**
     * 监听服务变化
     *
     * @param serviceName 服务名
     * @param listener 监听器
     */
    public fun subscribe(serviceName: String, listener: INotifyListener){
        try{
            // 监听子节点
            val childListener = ZkChildListener(listener)
            childListeners.getOrPut(serviceName){ // 记录监听器，以便取消监听时使用
                ConcurrentHashMap()
            }.put(listener, childListener)
            zkClient.subscribeChildChanges(serviceName, childListener)

            // 发现服务：获得子节点
            val urls = discover(serviceName)
            if(urls.isEmpty())
                return;

            // 监听子节点的数据变化
            val list = ArrayList<ZkDataListener>()
            for (url in urls){
                val dataListener = ZkDataListener(url, listener)
                list.add(dataListener)
                zkClient.subscribeDataChanges(url.nodePath, dataListener);
            }
            dataListeners.getOrPut(serviceName){ // 记录监听器，以便取消监听时使用
                ConcurrentHashMap()
            }.put(listener, list)
        } catch (e: Throwable) {
            throw SoaException(String.format("Failed to discover service %s from zookeeper, cause: %s", serviceName, e.message), e)
        }
    }

    /**
     * 取消监听服务变化
     *
     * @param serviceName 服务名
     * @param listener 监听器
     */
    public fun unsubscribe(serviceName: String, listener: INotifyListener){
        try{
            // 取消监听子节点
            zkClient.unsubscribeChildChanges(serviceName, childListeners[serviceName]!![listener]!!)
            // 取消监听子节点的数据变化
            for(dataListener in dataListeners[serviceName]!![listener]!!){
                val path = dataListener.url.nodePath
                zkClient.unsubscribeDataChanges(path, dataListener)
            }
        } catch (e: Throwable) {
            throw SoaException(String.format("Failed to discover service %s from zookeeper, cause: %s", serviceName, e.message), e)
        }
    }

    /**
     * 发现服务
     *
     * @param serviceName 服务名
     * @return 服务地址
     */
    public fun discover(serviceName: String): List<Url> {
        try {
            // 获得子节点
            var currentChilds: List<String> = emptyList()
            if (zkClient.exists(serviceName))
                currentChilds = zkClient.getChildren(serviceName)

            return zkClient.nodeChilds2Urls(serviceName, currentChilds)
        } catch (e: Throwable) {
            throw SoaException(String.format("Failed to discover service %s from zookeeper, cause: %s", serviceName, e.message), e)
        }

    }
}