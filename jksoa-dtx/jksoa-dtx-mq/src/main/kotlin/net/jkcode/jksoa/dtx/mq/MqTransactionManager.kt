package net.jkcode.jksoa.dtx.mq

import net.jkcode.jkutil.common.Config
import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.dtx.mq.model.MqTransactionModel
import net.jkcode.jkmq.mqmgr.IMqManager
import java.io.File
import java.io.InputStreamReader

/**
 * 事务消息的管理者
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
object MqTransactionManager : IMqTransactionManager {

    /**
     * 配置
     */
    public val config: Config = Config.instance("dtx-mq", "yaml")

    /**
     * 消息管理者
     */
    public val mqMgr = IMqManager.instance(config["mqType"]!!)

    init {
        // 初始化时建表: mq_transaction
        createTable(config["dbName"]!!)
    }

    /**
     * 建表: mq_transaction
     * @param db
     */
    private fun createTable(db: String) {
        val `is` = Thread.currentThread().contextClassLoader.getResourceAsStream("mq_transaction.mysql.sql")
        val sql = InputStreamReader(`is`).readText()
        Db.instance().execute(sql)
        dtxMqLogger.debug("建表: mq_transaction")
    }

    /**
     * 添加事务消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     */
    public override fun addMq(topic: String, msg: ByteArray, bizType: String, bizId: String) {
        // 1 保存消息事务
        val mq = MqTransactionModel()
        mq.topic = topic
        mq.msg = msg
        mq.bizType = bizType
        mq.bizId = bizId
        mq.created = System.currentTimeMillis() / 1000
        mq.nextSendTime = calculateNextSendTime(0)
        mq.create()

        // 2 在事务完成回调中发送消息
        MqTransactionModel.db.addTransactionCallback { commited ->
            if(commited)
                sendMq(mq)
        }
    }

    /**
     * 计算下一次发送时间
     * @param tryCount 重试次数
     * @return
     */
    private fun calculateNextSendTime(tryCount: Int): Long {
        val now: Long = System.currentTimeMillis() / 1000
        return now + config.getInt("retrySeconds", 10)!! * tryCount
    }

    /**
     * 处理单个消息发送
     * @param msg
     */
    public override fun sendMq(msg: MqTransactionModel) {
        // 更新下一次的重发时间: TODO: 可批量更新, 直接将 calculateNextSendTime()中的时间计算算法写成sql
        msg.nextSendTime = calculateNextSendTime(msg.tryCount + 1)
        msg.update()

        // 发送消息
        mqMgr.sendMq(msg.topic, msg.msg).whenComplete { r, ex ->
            if (ex == null) // 发送成功, 则删除事务消息
                msg.delete()
            else {  // 发送失败, 则更新重试次数
                msg.tryCount = msg.tryCount + 1
                msg.update()
            }
        }
    }


}