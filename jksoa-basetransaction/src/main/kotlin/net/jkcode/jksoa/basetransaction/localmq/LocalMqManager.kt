package net.jkcode.jksoa.basetransaction.localmq

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.basetransaction.mqsender.IMqSender
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.trigger.CronTrigger
import java.io.File

/**
 * 本地消息的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
object LocalMqManager : ILocalMqManager {

    /**
     * 配置
     */
    public val config: Config = Config.instance("basetransaction", "yaml")

    /**
     * 消息发送者
     */
    public val sender = IMqSender.instance(config["mqType"]!!)

    init {
        // 初始化时建表: local_mq
        val sqlFile = Thread.currentThread().contextClassLoader.getResource("local_mq.mysql.sql").getFile()
        val sql = File(sqlFile).readText()
        Db.instance().execute(sql)
    }

    /**
     * 添加本地消息
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     * @param topic 消息主题
     * @param msg 消息内容
     */
    public override fun addLocalMq(bizType: String, bizId: String, topic: String, msg: ByteArray) {
        val i = LocalMqModel()
        i.bizType = bizType
        i.bizId = bizId
        i.topic = topic
        i.msg = msg
        i.created = System.currentTimeMillis() / 1000
        i.save()
    }

    /**
     * 启动定时发送消息的作业
     */
    public override fun startSendMqJob(){
        // 定义job
        val job = RpcJob(::processLocalMq)
        // 定义trigger
        val trigger = CronTrigger("0/5 * * * * ?")
        // 给trigger添加要触发的job
        trigger.addJob(job)
        // 启动trigger, 开始定时触发作业
        trigger.start()
    }

    /**
     * 定时处理本地消息
     *   查询并发送消息
     */
    private fun processLocalMq(limit: Int){
        val msgs = LocalMqModel.queryBuilder().limit(limit).findAllModels<LocalMqModel>()
        for (msg in msgs) {
            sender.sendMq(msg.topic, msg.msg).whenComplete { r, ex ->
                if(ex == null)
                    LocalMqModel.delete(msg.id)
                else
                    LocalMqModel.db.execute("UPDATE `local_mq` SET `try_times`=`try_times`+1 WHERE `id`=?", listOf(msg.id))
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        startSendMqJob()
    }
}