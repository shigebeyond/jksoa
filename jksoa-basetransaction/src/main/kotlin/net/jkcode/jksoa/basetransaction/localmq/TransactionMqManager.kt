package net.jkcode.jksoa.basetransaction.localmq

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.basetransaction.baseTransactionLogger
import net.jkcode.jksoa.basetransaction.mqsender.IMqSender
import net.jkcode.jksoa.job.job.LambdaJob
import net.jkcode.jksoa.job.trigger.CronTrigger
import java.io.File

/**
 * 事务消息的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
object TransactionMqManager : ITransactionMqManager {

    /**
     * 配置
     */
    public val config: Config = Config.instance("basetransaction", "yaml")

    /**
     * 消息发送者
     */
    public val sender = IMqSender.instance(config["mqType"]!!)

    init {
        // 初始化时建表: transaction_mq
        createTable()

        // 启动定时发送消息的作业
        if(config["autoStartSendMqJob"]!!)
            startSendMqJob()
    }

    /**
     * 建表: transaction_mq
     */
    private fun createTable() {
        val sqlFile = Thread.currentThread().contextClassLoader.getResource("transaction_mq.mysql.sql").getFile()
        val sql = File(sqlFile).readText()
        Db.instance().execute(sql)
    }

    /**
     * 添加事务消息
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     * @param topic 消息主题
     * @param msg 消息内容
     */
    public override fun addMq(bizType: String, bizId: String, topic: String, msg: ByteArray) {
        val item = TransactionMqModel()
        item.bizType = bizType
        item.bizId = bizId
        item.topic = topic
        item.msg = msg
        item.created = System.currentTimeMillis() / 1000
        item.nextSendTime = calculateNextSendTime(0)
        item.save()
    }

    /**
     * 计算下一次发送时间
     * @param tryTimes 重试次数
     * @return
     */
    private fun calculateNextSendTime(tryTimes: Int): Long {
        val now: Long = System.currentTimeMillis() / 1000
        return now + config.getInt("sendSecondSpan", 10)!! * tryTimes
    }

    /**
     * 启动定时发送消息的作业
     */
    public override fun startSendMqJob(){
        // 定义job
        val job = LambdaJob{
            processLocalMq()
        }
        // 定义trigger
        val trigger = CronTrigger("0/5 * * * * ?")
        // 给trigger添加要触发的job
        trigger.addJob(job)
        // 启动trigger, 开始定时触发作业
        trigger.start()
    }

    /**
     * 定时处理事务消息
     *   查询并发送消息
     */
    private fun processLocalMq(){
        baseTransactionLogger.debug("定时处理事务消息")
        // 查询事务消息
        val limit: Int = config["sendPageSize"]!!
        val now: Long = System.currentTimeMillis() / 1000
        val msgs = TransactionMqModel.queryBuilder().where("next_send_time", "<=", now).limit(limit).findAllModels<TransactionMqModel>()
        for (msg in msgs) {
            // 更新下一次的重发时间: TODO: 可批量更新, 直接将 calculateNextSendTime()中的时间计算算法写成sql
            TransactionMqModel.db.execute("UPDATE `local_mq` SET next_send_time = ? WHERE `id`=?", listOf(msg.id, calculateNextSendTime(msg.tryTimes + 1)))

            // 发送消息
            sender.sendMq(msg.topic, msg.msg).whenComplete { r, ex ->
                if(ex == null) // 发送成功, 则删除事务消息
                    TransactionMqModel.delete(msg.id)
                else  // 发送失败, 则更新重试次数
                    TransactionMqModel.db.execute("UPDATE `local_mq` SET `try_times`=`try_times`+1 WHERE `id`=?", listOf(msg.id))
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        startSendMqJob()
    }

}