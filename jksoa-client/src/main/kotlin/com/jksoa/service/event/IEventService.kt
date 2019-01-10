package com.jksoa.service.event

import com.jksoa.common.IService

/**
 * 事件接口
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IEventService : IService  {

    /**
     * 通知事件
     * @param 事件
     */
    fun notifyEvent(event: Event)
}