package net.jkcode.jksoa.mq.registry

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