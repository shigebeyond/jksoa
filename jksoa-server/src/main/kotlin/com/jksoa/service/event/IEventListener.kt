package com.jksoa.service.event

/**
 * 事件监听器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
interface IEventListener {

    /**
     * 处理事件
     * @param event 事件
     */
    fun handleEvent(event: Event)
}