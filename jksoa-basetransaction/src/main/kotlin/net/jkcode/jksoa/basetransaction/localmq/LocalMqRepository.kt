package net.jkcode.jksoa.basetransaction.localmq

import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.trigger.CronTrigger
import java.io.File

/**
 * 本地消息的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
object LocalMqRepository : ILocalMqRepository {

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
    public override fun add(bizType: String, bizId: String, topic: String, msg: ByteArray) {
        val i = LocalMqModel()
        i.bizType = bizType
        i.bizId = bizId
        i.topic = topic
        i.msg = msg
        i.created = System.currentTimeMillis() / 1000
        i.save()
    }

    /**
     * 启动作业
     */
    public fun startJob(){
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
    public fun processLocalMq(limit: Int){
        val msgs = LocalMqModel.queryBuilder().limit(limit).findAllModels<LocalMqModel>()
        for (msg in msgs)
            sendMq(msg.topic, msg.msg)
    }

    /**
     * 删除本地消息
     * @param id
     */
    public override fun delete(id: Int) {
        LocalMqModel.delete(id)
    }

    /**
     * 查询待发送的本地消息
     * @param limit
     * @return
     */
    public override fun findAll(limit: Int): List<LocalMqModel> {
        return LocalMqModel.queryBuilder().limit(limit).findAllModels<LocalMqModel>()
    }

    /**
     * 增加重试次数
     * @param id
     */
    public override fun increaseTryTimes(id: Int) {
        LocalMqModel.db.execute("UPDATE `local_mq` SET `try_times`=`try_times`+1 WHERE `id`=?")
    }
}