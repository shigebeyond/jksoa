package com.jksoa.registry.zk

import com.jkmvc.common.Config
import com.jksoa.common.*
import com.jksoa.registry.IRegistry
import com.jksoa.common.SoaException
import com.jksoa.server.ProviderLoader
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
object ZkRegistry : IRegistry, ZkDiscovery() {

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
                for(provider in ProviderLoader.getProviders()){

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
}