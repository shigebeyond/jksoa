package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jkmq.mqmgr.kafka.KafkaMqManager
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jkutil.common.Config

/**
 * docker swarm模式下的服务节点信息的监听器
 *   订阅服务节点信息的mq，从而触发 IDiscoveryListener 的事件处理
 *   要检查kafka随机分组，这样才能接收广播mq
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
abstract class SwarmDiscoveryListener: IConnectionHub() {

    /**
     * swarm服务节点数
     */
    protected var swarmServiceReplicas: MutableMap<String, Int> = HashMap()

    init {
        // 检查kafka消费者配置
        checkMqConsumer()

        // 全局的订阅
        SwarmUtil.mqMgr.subscribeMq(SwarmUtil.topic){
            handleSwarmServiceReplicasChange(it as MutableMap<String, Int>)
        }
    }

    /**
     * 检查kafka消费者配置
     *   主要是检查随机分组，这样才能接收广播
     */
    private fun checkMqConsumer() {
        // 只检查kafka
        if(SwarmUtil.mqMgr !is KafkaMqManager)
            return

        val configName = SwarmUtil.mqMgr.name // mq配置名
        val config = Config.instance("kafka-consumer.$configName", "yaml")
        // 1 检查消费者分组
        val group: String? = config["group.id"]
        if (!group.isNullOrEmpty())
            throw RpcClientException("SwarmDiscoveryListener监听必须是随机分组，这样才能接收广播")
        // 2 检查并行的消费者数
        val concurrency: Int = config["concurrency"]!!
        if (concurrency > 1)
            throw RpcClientException("SwarmDiscoveryListener并行的消费者数要为1")
    }

    /**
     * 处理swarm服务节点数变化: 对比本地数据, 从而识别增删改, 从而触发 IDiscoveryListener 的增删改方法
     *
     * @param serviceId 服务标识
     * @param newData 服务节点数
     */
    public fun handleSwarmServiceReplicasChange(newData: MutableMap<String, Int>){
        var addServers:Set<String> = emptySet() // 新加的server
        var removeServers:Set<String> = emptySet() // 新加的server
        var updateServers:List<String> = emptyList() // 更新的server

        // 1 获得旧的服务节点数
        var oldData = this.swarmServiceReplicas

        // 2 比较新旧服务节点数，分别获得增删改的数据
        if(oldData.isEmpty()) {
            // 全是新加地址
            addServers = newData.keys
        }else{
            // 获得新加的地址
            addServers = newData.keys.subtract(oldData.keys)

            // 获得删除的地址
            removeServers = oldData.keys.subtract(newData.keys)

            // 获得更新的地址
            newData.keys.intersect(oldData.keys).filter { server ->
                newData[server] != oldData[server] // 节点数变化
            }
        }

        // 3 新加的地址
        for (server in addServers){
            val replica = newData[server]!!
            val url = SwarmUtil.swarmServer2Url(server, replica)
            handleServiceUrlAdd(url, emptyList())
        }

        // 4 删除的地址
        for(server in removeServers) {
            oldData.remove(server)
            val url = SwarmUtil.swarmServer2Url(server, 0)
            handleServiceUrlRemove(url, emptyList())
        }

        // 5 更新的地址
        for(server in updateServers) {
            val replica = newData[server]!!
            val url = SwarmUtil.swarmServer2Url(server, replica)
            handleParametersChange(url)
        }

        swarmServiceReplicas = newData
    }
}