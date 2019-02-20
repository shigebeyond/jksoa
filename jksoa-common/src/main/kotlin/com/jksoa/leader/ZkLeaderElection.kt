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
import java.util.*

/**
 * 选举领导者: 基于zk的顺序节点来实现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class ZkLeaderElection(public override val teamName: String /* 团队名 */,
                       public override val myData: String = Application.fullWorkerId /* 我的数据 */,
                       public override val callback: (ZkLeaderElection)->Unit /* 成功回调 */
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
     * 我是否是leader
     */
    public override var isLeader: Boolean = false
        protected set

    /**
     * 领导者的数据
     */
    public override var leaderData: String = ""
        protected set

    /**
     * zk的父节点路径
     */
    protected val parentPath: String = "${RootPath}/$teamName"

    /**
     * zk的当前节点路径
     */
    protected var path: String = ""

    /**
     * zk的当前节点序号
     */
    protected val no: String
        get() = path.substring(parentPath.length + 1)

    /**
     * zk的前一个节点路径
     */
    protected var prePath: String? = null

    /**
     * 前一个节点的事件监听器
     */
    protected val dataListener:IZkDataListener = object : IZkDataListener {

        /**
         * 处理zk中节点数据删除事件
         */
        override fun handleDataDeleted(dataPath: String) {
            // 识别领导者
            identifyLeader()
        }

        /**
         * 处理zk中节点数据变化事件
         */
        override fun handleDataChange(dataPath: String, data: Any) {
        }
    }

    /**
     * 参选
     * @return 是否当选
     */
    public override fun run(): Boolean {
        // 创建根节点
        if (!zkClient.exists(parentPath))
            try{
                zkClient.createPersistent(parentPath, true)
            } catch (e: ZkNodeExistsException) {
                // do nothing
            }

        // 创建顺序节点
        path = zkClient.createEphemeralSequential(parentPath + "/", myData)
        commonLogger.debug("团队[$teamName]的节点[$myData]路径: $path")

        // 识别领导者
        return identifyLeader()
    }

    /**
     * 识别领导者
     * @return
     */
    protected fun identifyLeader(): Boolean {
        // 获得所有顺序节点
        val childrenNos = zkClient.getChildren(parentPath)
        Collections.sort(childrenNos)

        // 检查本机是否是最小的
        val i = childrenNos.indexOf(no)
        if (i == 0) {
            commonLogger.debug("团队[$teamName]的节点[$myData]被选为领导者")
            // 成为领导者
            isLeader = true
            // 设置领导者的数据
            leaderData = myData
            // 成功回调
            callback(this)
            return true
        }

        // 获得领导者的数据
        leaderData = zkClient.readData("$parentPath/${childrenNos.first()}")

        // 取消之前订阅
        if(prePath != null)
            zkClient.unsubscribeDataChanges(prePath, dataListener)

        // 订阅前一个节点
        val preChildNo = childrenNos.get(i - 1)
        prePath = "$parentPath/$preChildNo"
        commonLogger.debug("团队[$teamName]的落选节点[$myData]订阅前一个节点: $prePath")
        zkClient.subscribeDataChanges(prePath, dataListener)
        return false
    }

    /**
     * 关闭选举
     */
    public override fun close() {
        // 取消订阅
        if(prePath != null)
            zkClient.unsubscribeDataChanges(prePath, dataListener)
    }

}
