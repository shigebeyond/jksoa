package com.jksoa.registry.zk

import com.jksoa.common.Url
import com.jksoa.common.registerLogger
import com.jksoa.registry.IRegistry
import com.jksoa.registry.RegistryException
import org.I0Itec.zkclient.IZkStateListener
import org.apache.zookeeper.Watcher

/**
 * 基于zookeeper的注册中心
 *
 * @ClassName: ZkRegistry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
object ZkRegistry : IRegistry, ZkDiscovery() {

    /**
     * 注册过的服务地址
     */
    private val serviceUrls = HashSet<Url>()

    init {
        // 添加连接监听
        val zkStateListener = object : IZkStateListener {

            override fun handleSessionEstablishmentError(error: Throwable?) {
            }

            override fun handleStateChanged(state: Watcher.Event.KeeperState) {
            }

            override fun handleNewSession() {
                try {
                    registerLogger.info("zkRegistry get new session handleNotify.")
                    // 重新注册服务（都是本地自己提供的服务）
                    for (url in serviceUrls) {
                        register(url)
                    }
                }catch(e: Exception){
                    registerLogger.error("处理zk会话建立事件失败", e)
                    throw e
                }
            }
        }
        zkClient.subscribeStateChanges(zkStateListener)
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
            // 记录地址
            serviceUrls.add(url)
        } catch (e: Throwable) {
            throw RegistryException(String.format("Failed to discover service %s from zookeeper, cause: %s", url, e.message), e)
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
            // 删除地址
            serviceUrls.remove(url)
        } catch (e: Throwable) {
            throw RegistryException(String.format("Failed to discover service %s from zookeeper, cause: %s", url, e.message), e)
        }
    }

    /**
     * 创建节点
     *
     * @param url
     */
    private fun createNode(url: Url) {
        // 创建根节点
        val root = url.rootPath
        if (!zkClient.exists(root))
            zkClient.createPersistent(root, true)

        // 创建子节点: Ephemeral节点，session长的生命周期，zk自动通过心跳保持会话
        zkClient.createEphemeral(url.childPath, url.toString())
    }

    /**
     * 删除节点
     *
     * @param url
     */
    private fun removeNode(url: Url) {
        // 删除节点
        val path = url.childPath
        if (zkClient.exists(path))
            zkClient.delete(path)
    }
}