package net.jkcode.jksoa.basetransaction.localmq

/**
 * 本地消息的仓库
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 5:36 PM
 */
interface ILocalMqRepository {
    /**
     * 添加本地消息
     * @param bizType 业务类型
     * @param bizId 业务主体编号
     * @param topic 消息主题
     * @param msg 消息内容
     */
    fun add(bizType: String, bizId: String, topic: String, msg: ByteArray)

    /**
     * 删除本地消息
     * @param id
     */
    fun delete(id: Int)

    /**
     * 查询待发送的本地消息
     * @param limit
     * @return
     */
    fun findAll(limit: Int): List<LocalMqModel>

    /**
     * 增加重试次数
     * @param id
     */
    fun increaseTryTimes(id: Int)
}