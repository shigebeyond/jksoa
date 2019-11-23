package net.jkcode.jksoa.dtx.mq

import net.jkcode.jkutil.common.CommonSecondTimer
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.newPeriodic
import net.jkcode.jksoa.dtx.mq.model.MqTransactionModel
import java.util.concurrent.TimeUnit

/**
 * 事务消息的恢复(重试)机制
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-22 4:45 PM
 */
object MqTransactionRecovery {

    /**
     * 配置
     */
    private val config: Config = Config.instance("dtx-mq", "yaml")

    /**
     * 定时时间间隔
     */
    private val timerSeconds: Long = config["recoverTimerSeconds"]!!

    init{
        // 启动定时恢复(重发消息)
        if(timerSeconds > 0)
            start(timerSeconds)
    }

    /**
     * 恢复: 重发到期消息
     */
    private fun recover(){
        // 查询事务消息
        val limit: Int = MqTransactionManager.config["sendPageSize"]!!
        val now: Long = System.currentTimeMillis() / 1000
        val msgs = MqTransactionModel.queryBuilder().where("next_send_time", "<=", now).limit(limit).findAllModels<MqTransactionModel>()
        // 发送消息
        dtxMqLogger.debug("定时重发 {} 个事务消息", msgs.size)
        for (msg in msgs)
            MqTransactionManager.sendMq(msg)
    }

    /**
     * 启动定时恢复(重发消息)
     * @param timerSeconds
     */
    public fun start(timerSeconds: Long){
        CommonSecondTimer.newPeriodic({
            recover()
        }, timerSeconds, TimeUnit.SECONDS)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        start(timerSeconds)
    }
}