package net.jkcode.jksoa.mq.registry

/**
 * mq服务发现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
interface IMqDiscovery {
    /**
     * 监听topic分配变化
     *
     * @param listener 监听器
     */
    fun subscribe(listener: IMqDiscoveryListener)

    /**
     * 取消topic分配变化
     *
     * @param listener 监听器
     */
    fun unsubscribe(listener: IMqDiscoveryListener)

    /**
     * 发现topic分配
     *
     * @return 服务地址
     */
    fun discover(): TopicAssignment
}