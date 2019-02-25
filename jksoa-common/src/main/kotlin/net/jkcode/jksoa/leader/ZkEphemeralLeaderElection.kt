package net.jkcode.jksoa.leader

import com.jkmvc.closing.ClosingOnShutdown
import com.jkmvc.common.Application
import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import net.jkcode.jksoa.common.commonLogger
import net.jkcode.jksoa.common.zk.ZkClientFactory
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.IZkDataListener
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import java.util.*

/**
 * 选举领导者: 基于zk的顺序节点来实现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class ZkEphemeralLeaderElection(public override val teamName: String /* 团队名 */,
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
     * zk的父节点路径
     */
    protected val parentPath: String = "${RootPath}/$teamName"

    /****************************** 监听处理 *******************************/
    /**
     * 子节点变化监听器
     */
    protected var childListener: IZkChildListener? = null

    /**
     * 监听选举结果
     * @param callback 选举结果的回调
     */
    public override fun listen(callback: (String)->Unit){
        // 1 先查结果
        val childrenNos = zkClient.getChildren(parentPath)
        val leaderData = getLeaderData(childrenNos)
        // 选举结果回调
        callback(leaderData)

        // 2 监听子节点变化
        childListener = object: IZkChildListener{
            // 处理zk中子节点变化事件
            override fun handleChildChange(parentPath: String, childrenNos: List<String>) {
                // 获得领导者的数据
                val leaderData = getLeaderData(childrenNos)
                // 选举结果回调
                callback(leaderData)
            }
        }
        zkClient.subscribeChildChanges(parentPath, childListener)
    }

    /**
     * 根据子节点列表, 获得领导者数据
     * @param childrenNos 子节点列表
     * @return
     */
    protected fun getLeaderData(childrenNos: List<String>): String {
        Collections.sort(childrenNos)
        // 获得领导者的数据
        val leaderData: String = zkClient.readData("$parentPath/${childrenNos.first()}")
        return leaderData
    }

    /****************************** 竞选处理 *******************************/
    /**
     * 前一个节点路径
     */
    protected var prePath: String? = null

    /**
     * 前一个节点的事件监听器
     */
    protected var preDataListener:IZkDataListener? = null

    /**
     * 参选
     * @param callback 成功回调
     */
    public override fun run(callback: ()->Unit) {
        // 创建根节点
        if (!zkClient.exists(parentPath))
            try{
                zkClient.createPersistent(parentPath, true)
            } catch (e: ZkNodeExistsException) {
                // do nothing
            }

        // 创建顺序节点
        val path = zkClient.createEphemeralSequential(parentPath + "/", data)
        commonLogger.debug("团队[$teamName]的竞选节点[$data]的路径: $path")

        // 识别领导者
        identifyLeaderNode(path, callback)
    }

    /**
     * 识别领导者
     * @param path 当前节点路径
     * @param callback 成功回调
     * @return
     */
    protected fun identifyLeaderNode(path: String, callback: ()->Unit): Boolean {
        // 当前节点序号
        val no = path.substring(parentPath.length + 1)

        // 获得所有顺序节点
        val childrenNos = zkClient.getChildren(parentPath)
        Collections.sort(childrenNos)

        // 检查本机是否是最小的
        val i = childrenNos.indexOf(no)
        if (i == 0) {
            commonLogger.debug("团队[$teamName]的节点[$data]被选为领导者")
            // 成功回调
            callback()
            return true
        }

        // 取消之前订阅
        if(prePath != null)
            zkClient.unsubscribeDataChanges(prePath, preDataListener)

        // 订阅前一个节点
        preDataListener = object : IZkDataListener {
            // 处理zk中节点数据删除事件
            override fun handleDataDeleted(dataPath: String) {
                // 识别领导者
                identifyLeaderNode(path, callback)
            }

            // 处理zk中节点数据变化事件
            override fun handleDataChange(dataPath: String, data: Any) {
            }
        }
        val preChildNo = childrenNos.get(i - 1)
        prePath = "$parentPath/$preChildNo"
        commonLogger.debug("团队[$teamName]的落选节点[$data]订阅前一个节点: $prePath")
        zkClient.subscribeDataChanges(prePath, preDataListener)
        return false
    }

    /**
     * 关闭选举
     */
    public override fun close() {
        // 取消订阅
        if(childListener != null)
            zkClient.unsubscribeChildChanges(parentPath, childListener)
        if(prePath != null)
            zkClient.unsubscribeDataChanges(prePath, preDataListener)
    }

}
