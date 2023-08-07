package net.jkcode.jksoa.rpc.client.k8s

import net.jkcode.jkmq.mqmgr.kafka.KafkaMqManager
import net.jkcode.jksoa.common.k8sLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.removeBy

/**
 * k8s模式下的k8s应用节点数的监听器
 *   订阅k8s应用节点数的mq，从而触发 IDiscoveryListener 的事件处理
 *   要检查kafka随机分组，这样才能接收广播mq
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
abstract class K8sDiscoveryListener: IConnectionHub() {

    /**
     * 服务标识，即接口类全名
     */
    public override val serviceId: String
        get() = throw RpcClientException("k8s发现的是应用, 而不是单个服务接口")

    /**
     * client关注哪些k8s命名空间
     */
    protected var k8sNs = config.get("k8sns", "default")!!.split(',').map { '.' + it }

    /**
     * k8s应用节点数
     */
    @Volatile
    protected var k8sServiceReplicas: MutableMap<String, Int> = HashMap()

    init {
        // 检查kafka消费者配置
        checkMqConsumer()

        // 全局的订阅
        k8sLogger.debug("K8sDiscoveryListener订阅k8s应用节点数的mq")
        K8sUtil.mqMgr.subscribeMq(K8sUtil.topic, this::handleK8sServiceReplicasChange)
    }

    /**
     * 检查kafka消费者配置
     *   主要是检查随机分组，这样才能接收广播
     */
    private fun checkMqConsumer() {
        // 只检查kafka
        if(K8sUtil.mqMgr !is KafkaMqManager)
            return

        val configName = K8sUtil.mqMgr.name // mq配置名
        val config = Config.instance("kafka-consumer.$configName", "yaml")
        // 1 检查消费者分组
        val group: String? = config["group.id"]
        if (!group.isNullOrEmpty())
            throw RpcClientException("K8sDiscoveryListener监听必须是随机分组，这样才能接收广播")
        // 2 检查并行的消费者数
        val concurrency: Int = config["concurrency"]!!
        if (concurrency > 1)
            throw RpcClientException("K8sDiscoveryListener并行的消费者数要为1")
    }

    /**
     * 检查某个服务是否在我关注的命名空间中
     */
    protected fun checkInK8sNs(server: String): Boolean {
        return k8sNs.any {
            server.endsWith(it)
        }
    }

    /**
     * 处理k8s应用节点数变化: 对比本地数据, 从而识别增删改, 从而触发 IDiscoveryListener 的增删改方法
     * @param data Map<k8s应用域名, 节点数>
     */
    public fun handleK8sServiceReplicasChange(data: Any){
        val newData = data as MutableMap<String, Int>
        // 删除无关命名空间的server
        newData.removeBy {
            !checkInK8sNs(it.key)
        }

        if(newData == this.k8sServiceReplicas){
            k8sLogger.debug("K8sDiscoveryListener收到k8s应用节点数的mq, 未发现变化: {}", newData)
            return
        }

        k8sLogger.debug("K8sDiscoveryListener收到k8s应用节点数的mq: {}", newData)

        // 计算增删改的server
        var addServers:Set<String> = emptySet() // 新加的server
        var removeServers:Set<String> = emptySet() // 新加的server
        var updateServers:List<String> = emptyList() // 更新的server

        // 1 获得旧的应用节点数
        var oldData = this.k8sServiceReplicas

        // 2 比较新旧应用节点数，分别获得增删改的数据
        if(oldData.isEmpty()) {
            // 全是新加地址
            addServers = newData.keys
        }else{
            // 获得新加的地址
            addServers = newData.keys.subtract(oldData.keys)

            // 获得删除的地址
            removeServers = oldData.keys.subtract(newData.keys)

            // 获得更新的地址
            updateServers = newData.keys.intersect(oldData.keys).filter { server ->
                newData[server] != oldData[server] // 节点数变化
            }
        }

        // 3 新加的地址
        for (server in addServers){
            val replica = newData[server]!!
            val url = K8sUtil.k8sServer2Url(server, replica)
            handleServiceUrlAdd(url, emptyList())
        }

        // 4 删除的地址
        for(server in removeServers) {
            oldData.remove(server)
            val url = K8sUtil.k8sServer2Url(server, 0)
            handleServiceUrlRemove(url, emptyList())
        }

        // 5 更新的地址
        for(server in updateServers) {
            val replica = newData[server]!!
            val url = K8sUtil.k8sServer2Url(server, replica)
            handleParametersChange(url)
        }

        k8sServiceReplicas = newData
    }

}