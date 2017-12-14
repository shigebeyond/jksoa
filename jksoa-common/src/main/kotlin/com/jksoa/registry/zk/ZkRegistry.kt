package com.jksoa.registry.zk

import com.jkmvc.common.Config
import com.jksoa.common.*
import com.jksoa.registry.IRegistry
import com.jksoa.common.SoaException
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.IZkDataListener
import org.I0Itec.zkclient.IZkStateListener
import org.I0Itec.zkclient.ZkClient
import org.apache.zookeeper.Watcher
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于zookeeper的注册中心
 *
 * @ClassName: ZkRegistry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object ZkRegistry : IRegistry {

    /**
     * zk客户端
     */
    private val zkClient: ZkClient = ZkClientFactory.instance()

    /**
     * zk子节点监听器: <服务名 to <服务监听器 to zk监听器>>
     */
    private val childListeners = ConcurrentHashMap<String, ConcurrentHashMap<INotifyListener, ZkChildListener>>()

    /**
     * zk节点数据监听器: <服务名 to <服务监听器 to zk监听器>>
     */
    private val dataListeners = ConcurrentHashMap<String, ConcurrentHashMap<INotifyListener, List<ZkDataListener>>>()

    init {
        // 添加连接监听
        val zkStateListener = object : IZkStateListener {

            override fun handleSessionEstablishmentError(error: Throwable?) {
            }

            override fun handleStateChanged(state: Watcher.Event.KeeperState) {
            }

            override fun handleNewSession() {
                soaLogger.info("zkRegistry get new session handleNotify.")
                // 重新注册服务
            }
        }
        zkClient.subscribeStateChanges(zkStateListener)
    }

    /**
     * 创建节点
     *
     * @param url
     */
    private fun createNode(url: Url) {
        // 创建目录
        val dir = url.serviceName
        if (!zkClient.exists(dir))
            zkClient.createPersistent(dir, true)

        // 创建节点
        zkClient.createEphemeral(url.path, url.toString())
    }

    /**
     * 删除节点
     *
     * @param url
     */
    private fun removeNode(url: Url) {
        // 删除节点
        val path = url.nodePath
        if (zkClient.exists(path))
            zkClient.delete(path)
    }

    /**
     * 注册服务
     *
     * @param url
     * @return
     */
    public override fun register(url: Url) {
        try{
            // 删除旧的节点
            removeNode(url)
            // 创建新节点
            createNode(url)
        } catch (e: Throwable) {
            throw SoaException(String.format("Failed to discover service %s from zookeeper, cause: %s", url, e.message), e)
        }
    }

    /**
     * 注销服务
     *
     * @param url
     * @return
     */
    public override fun unregister(url: Url) {
        try{
            // 删除节点
            removeNode(url)
        } catch (e: Throwable) {
            throw SoaException(String.format("Failed to discover service %s from zookeeper, cause: %s", url, e.message), e)
        }
    }

    /**
     * 监听服务变化
     *
     * @param serviceName 服务名
     * @param listener 监听器
     */
    public override fun subscribe(serviceName: String, listener: INotifyListener){
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
    public override fun unsubscribe(serviceName: String, listener: INotifyListener){
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
    public override fun discover(serviceName: String): List<Url>{
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