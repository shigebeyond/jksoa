package net.jkcode.jksoa.leader

import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.common.commonLogger
import net.jkcode.jksoa.zk.ZkClientFactory
import org.I0Itec.zkclient.IZkDataListener
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNoNodeException
import org.I0Itec.zkclient.exception.ZkNodeExistsException

/**
 * 选举领导者: 基于zk的单个临时节点来实现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class ZkLeaderElection(public override val module: String /* 模块 */,
                       public override val memberData: String = Application.fullWorkerId /* 数据 */
) : ILeaderElection, ClosingOnShutdown() {

    companion object {
        /**
         * zk根节点路径
         */
        public val RootPath: String = "/leader"

        /**
         * zk客户端
         */
        public val zkClient: ZkClient = ZkClientFactory.instance()

        init {
            // 创建根节点
            if (!zkClient.exists(RootPath))
                try{
                    zkClient.createPersistent(RootPath, true)
                } catch (e: ZkNodeExistsException) {
                    // do nothing
                }
        }
    }

    /**
     * zk的节点路径
     */
    protected val path: String = "${RootPath}/$module"

    /****************************** 监听处理 *******************************/
    /**
     * 节点的事件监听器
     */
    protected var dataChangeListener:IZkDataListener? = null

    /**
     * 监听选举结果
     * @param callback 选举结果的回调
     */
    public override fun listen(callback: (String)->Unit){
        // 1 先查结果
        try {
            val leaderData: String = zkClient.readData(path)
            callback(leaderData)
        } catch (e: ZkNoNodeException) {
            // do nothing
        }

        // 2 订阅节点变化
        dataChangeListener = object : IZkDataListener {
            // 处理zk中节点数据删除事件
            override fun handleDataDeleted(dataPath: String) {
            }

            // 处理zk中节点数据变化事件
            override fun handleDataChange(dataPath: String, data: Any) {
                callback(data as String)
            }
        }
        zkClient.subscribeDataChanges(path, dataChangeListener)
    }

    /****************************** 竞选处理 *******************************/
    /**
     * 节点的事件监听器
     */
    protected var dataDeleteListener:IZkDataListener? = null

    /**
     * 参选
     * @param callback 成功回调
     */
    public override fun run(callback: ()->Unit) {
        // 创建leader节点
        createLeaderNode(callback)
    }

    /**
     * 创建leader节点
     * @param callback 成功回调
     */
    protected fun createLeaderNode(callback: () -> Unit) {
        try {
            // 创建临时节点
            zkClient.createEphemeral(path, memberData)
            commonLogger.debug("模块[{}]的当选leader为[{}]", module, memberData)
            // 成功回调
            callback()
        } catch (e: ZkNodeExistsException) {
            if(dataDeleteListener != null)
                return

            // 订阅节点删除: 重新创建leader节点
            dataDeleteListener = object : IZkDataListener {
                // 处理zk中节点数据删除事件
                override fun handleDataDeleted(dataPath: String) {
                    // 创建leader节点
                    createLeaderNode(callback)
                }

                // 处理zk中节点数据变化事件
                override fun handleDataChange(dataPath: String, data: Any) {
                }
            }
            zkClient.subscribeDataChanges(path, dataDeleteListener)
        }
    }

    /**
     * 关闭选举
     */
    public override fun close() {
        // 取消订阅
        if(dataChangeListener != null)
            zkClient.unsubscribeDataChanges(path, dataChangeListener)
        if(dataDeleteListener != null)
            zkClient.unsubscribeDataChanges(path, dataDeleteListener)
    }

}
