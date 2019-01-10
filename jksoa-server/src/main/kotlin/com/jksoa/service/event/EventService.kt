package com.jksoa.service.event

import com.jksoa.client.Referer
import java.util.concurrent.ConcurrentHashMap

/**
 * 事件接口
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class EventService : IEventService  {

    companion object {

        /**
         * 添加事件监听器
         * @param name 事件名
         * @param listener
         */
        public fun addEventListener(name: String, listener: IEventListener){
            // 本地服务
            val localEventService = Referer.getRefer<IEventService>(true) as EventService
            // 添加事件监听器
            localEventService.addEventListener(name, listener)
        }
    }

    /**
     * 事件监听器: <事件名 to 监听器列表>
     */
    protected val listeners: ConcurrentHashMap<String, MutableList<IEventListener>> = ConcurrentHashMap();

    /**
     * 添加事件监听器
     * @param name 事件名
     * @param l
     */
    protected fun addEventListener(name: String, l: IEventListener){
        val ls = listeners.getOrPut(name){
            ArrayList()
        }
        ls.add(l)
    }

    /**
     * 通知事件
     * @param 事件
     */
    public override fun notifyEvent(event: Event){
        for(l in listeners.get(event.name)!!)
            l.handleEvent(event)
    }
}