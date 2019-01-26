package com.jksoa.lock


import com.jksoa.common.CommonTimer
import com.jksoa.common.zk.ZkClientFactory
import io.netty.util.Timeout
import io.netty.util.TimerTask

import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNoNodeException
import java.util.concurrent.TimeUnit

class ZkDLock(public override val name: String, /* 锁标识 */
              protected val configName: String = "default" /* redis配置名 */
) : IDLock() {

    companion object {

        /**
         * zk节点路径的前缀
         */
        public val PathPrefix: String = "/lock/"

    }

    /**
     * zk的锁节点路径
     */
    protected val path: String = "$PathPrefix.$name"

    /**
     * zk客户端
     */
    protected val client: ZkClient = ZkClientFactory.instance(configName)

    /**
     * 定时任务
     */
    protected var expireTimeout: Timeout? = null

    /**
    * 尝试加锁
    *
    * @param expireSeconds 锁的过期时间, 单位秒
    * @return
    */
    public override fun attemptLock(expireSeconds: Int): Boolean{
        try {
            // 创建临时节点
            client.createEphemeralSequential(path, null)
            // 记录过期时间
            expireTime = System.currentTimeMillis() + expireSeconds * 1000
            // 添加过期定时器
            expireTimeout = CommonTimer.newTimeout(object : TimerTask {
                override fun run(timeout: Timeout) {
                    // 清空定时任务
                    expireTimeout = null
                    // 释放锁
                    unlock()
                }
            }, expireSeconds.toLong(), TimeUnit.SECONDS)
            return true
        } catch (e: ZkNoNodeException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 解锁
     */
    public override fun unlock(){
        if(locked) { // 未过期
            // 删除临时节点
            client.delete(path)
            // 停止定时任务
            if(expireTimeout != null)
                expireTimeout!!.cancel()
        }

        expireTime = null
    }

}
