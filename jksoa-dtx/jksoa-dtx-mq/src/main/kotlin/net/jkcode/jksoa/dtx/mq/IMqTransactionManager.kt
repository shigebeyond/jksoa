package net.jkcode.jksoa.dtx.mq

import net.jkcode.jkutil.common.Application
import net.jkcode.jksoa.dtx.mq.model.MqTransactionModel

/**
 * 事务消息的管理者
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
interface IMqTransactionManager {

    /**
     * 添加事务消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     */
    fun addMq(topic: String, msg: ByteArray, bizType: String = Application.name, bizId: String = "")

    /**
     * 处理单个消息发送
     * @param msg
     */
    fun sendMq(msg: MqTransactionModel)

}