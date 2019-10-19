package net.jkcode.jksoa.rpc.registry.zk

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.rpc.registry.IRegistry
import net.jkcode.jksoa.rpc.registry.RegistryException
import org.I0Itec.zkclient.IZkStateListener
import org.apache.zookeeper.Watcher

/**
 * 基于zookeeper的注册中心
 *
 * zk目录结构如下:
 * ```
 * 	jksoa
 * 		net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
 * 			jkr:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
 * 			jkr:192.168.0.1:8080
 * 		net.jkcode.jksoa.rpc.example.ISimpleService
 * 			jkr:192.168.0.1:8080
 * 			jkr:192.168.0.1:8080
 * ```
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
class ZkRegistry : IRegistry, ZkDiscovery() {

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
                    registerLogger.info("ZkRegistry处理zk会话建立事件")
                    // 重新注册服务（都是本地自己提供的服务）
                    for (url in serviceUrls) {
                        register(url)
                    }
                }catch(e: Exception){
                    registerLogger.error("ZkRegistry处理zk会话建立事件失败", e)
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
        val root = url.serviceRegistryPath
        if (!zkClient.exists(root))
            zkClient.createPersistent(root, true)

        // 创建子节点: Ephemeral节点，session长的生命周期，zk自动通过心跳保持会话
        zkClient.createEphemeral(url.serverRegistryPath, url.getQueryString())
    }

    /**
     * 删除节点
     *
     * @param url
     */
    private fun removeNode(url: Url) {
        // 删除节点
        val path = url.serverRegistryPath
        if (zkClient.exists(path))
            zkClient.delete(path)
    }
}