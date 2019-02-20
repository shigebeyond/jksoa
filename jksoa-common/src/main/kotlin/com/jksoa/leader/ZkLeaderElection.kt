package com.jksoa.leader

import com.jkmvc.closing.ClosingOnShutdown
import com.jkmvc.common.Application
import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.commonLogger
import com.jksoa.common.zk.ZkClientFactory
import org.I0Itec.zkclient.IZkDataListener
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException

/**
 * 选举领导者: 基于zk的单个临时节点来实现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class ZkLeaderElection(public override val teamName: String /* 团队名 */,
                       public override val data: String = Application.fullWorkerId /* 数据 */
) : ILeaderElection, ClosingOnShutdown() {

    companion object {
        /**
         * zk根节点路径
         */
        public val RootPath: String = "/leader"

        /**
         * 配置
         */
        public val config: IConfig = Config.instance("leader", "yaml")

        /**
         * zk客户端
         */
        public val zkClient: ZkClient = ZkClientFactory.instance(config["zkConfigName"]!!)
    }

    /**
     * zk的节点路径
     */
    protected val path: String = "${RootPath}/$teamName"

    /**
     * 节点的事件监听器
     */
    protected var dataListener:IZkDataListener? = null

    /**
     * 监听选举结果
     * @param callback 选举结果的回调
     */
    public override fun listen(callback: (String)->Unit){
        // 订阅节点
        dataListener = object : IZkDataListener {
            // 处理zk中节点数据删除事件
            override fun handleDataDeleted(dataPath: String) {
            }

            // 处理zk中节点数据变化事件
            override fun handleDataChange(dataPath: String, data: Any) {
                callback(data as String)
            }
        }
        zkClient.subscribeDataChanges(path, dataListener)
    }

    /**
     * 参选
     * @param callback 成功回调
     */
    public override fun run(callback: ()->Unit) {
        // 创建根节点
        if (!zkClient.exists(RootPath))
            try{
                zkClient.createPersistent(RootPath, true)
            } catch (e: ZkNodeExistsException) {
                // do nothing
            }

        try{
            // 创建临时节点
            zkClient.createEphemeral(path, data)
            commonLogger.debug("团队[$teamName]的当选数据为[$data]")
            // 成功回调
            callback()
        } catch (e: ZkNodeExistsException) {
            // do nothing
        }
    }

    /**
     * 关闭选举
     */
    public override fun close() {
        // 取消订阅
        if(dataListener != null)
            zkClient.unsubscribeDataChanges(path, dataListener)
    }

}
