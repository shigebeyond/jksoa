package net.jkcode.jksoa.lock


import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.CommonSecondTimer
import net.jkcode.jksoa.common.zk.ZkClientFactory
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import java.util.concurrent.TimeUnit

/**
 * 分布式锁实现: zk临时节点
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-11 12:24 PM
 */
class ZkDLock(public override val name: String /* 锁标识 */,
              public override val data: String = Application.fullWorkerId /* 数据 */
) : IDLock() {

    companion object {

        /**
         * zk根节点路径
         */
        public val RootPath: String = "/lock"

        /**
         * 配置
         */
        public val config: IConfig = Config.instance("dlock", "yaml")

        /**
         * zk客户端
         */
        public val zkClient: ZkClient = ZkClientFactory.instance(config["zkConfigName"]!!)

        init{
            // 创建根节点
            if (!zkClient.exists(RootPath))
                zkClient.createPersistent(RootPath, true)
        }
    }

    /**
     * zk的锁节点路径
     */
    protected val path: String = "$RootPath/$name"

    /**
     * 过期的定时任务
     */
    protected var expireTimeout: Timeout? = null

    /**
    * 尝试加锁
    *
    * @param expireSeconds 锁的过期时间, 单位秒
    * @return
    */
    public override fun attemptLock(expireSeconds: Int): Boolean{
        if(locked) {
            // 更新过期任务
            refreshExpireTimeout(expireSeconds)
            return true
        }

        try {
            // 创建临时节点
            zkClient.createEphemeral(path, data)
            // 更新过期时间
            updateExpireTime(expireSeconds)
            // 更新过期定时器
            refreshExpireTimeout(expireSeconds)
            return true
        } catch (e: ZkNodeExistsException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * 更新过期任务
     *
     * @param expireSeconds 锁的过期时间, 单位秒
     */
    protected fun refreshExpireTimeout(expireSeconds: Int) {
        // 取消旧的过期任务
        expireTimeout?.cancel()
        // 创建新的过期任务
        expireTimeout = CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                // 清空定时任务
                expireTimeout = null
                // 释放锁
                unlock()
            }
        }, expireSeconds.toLong(), TimeUnit.SECONDS)
    }

    /**
     * 解锁
     */
    public override fun unlock(){
        if(locked) { // 未过期
            // 删除临时节点
            zkClient.delete(path)
            // 停止定时任务
            expireTimeout?.cancel()
        }

        expireTime = null
    }

}
