package net.jkcode.jksoa.basetransaction.localmq

/**
 * 事务消息的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
interface ITransactionMqManager {

    /**
     * 添加事务消息
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     * @param topic 消息主题
     * @param msg 消息内容
     */
    fun addMq(bizType: String, bizId: String, topic: String, msg: ByteArray)

    /**
     * 启动定时发送消息的作业
     */
    fun startSendMqJob()
}