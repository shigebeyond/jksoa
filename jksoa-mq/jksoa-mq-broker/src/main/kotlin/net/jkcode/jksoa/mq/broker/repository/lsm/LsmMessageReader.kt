package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.Store
import net.jkcode.jksoa.mq.broker.repository.IMessageRepository
import net.jkcode.jksoa.mq.common.GroupSequence
import net.jkcode.jksoa.mq.common.Message
import java.util.*
import kotlin.collections.ArrayList

/**
 * 消息的仓库 -- 读处理
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 9:59 AM
 */
abstract class LsmMessageReader : IMessageRepository {

    /**
     * 队列存储
     *   子目录是queue
     *   key是消息id, value是消息
     */
    protected lateinit var queueStore: Store<Long, Message>

    /**
     * 索引存储
     *   子目录是index
     *   key是消息id, value是待消费的分组id比特集合
     */
    protected lateinit var indexStore: Store<Long, BitSet>

    /**
     * 进度存储
     *   子目录是progress
     *   key为分组id, value是读进度对应的消息id
     */
    protected lateinit var progressStore: Store<Int, Long>

    /**
     * 根据范围查询多个消息
     * @param startId 开始的id
     * @param limit
     * @param inclusive 是否包含开始的id
     * @return
     */
    public override fun getMessagesByRange(startId: Long, limit: Int, inclusive: Boolean): List<Message> {
        // 获得按key有序的消息迭代器
        val itr = queueStore.iterator(startId, inclusive)
        var i = 0
        val result = ArrayList<Message>()
        while (itr.hasNext() && i < limit) {
            val msg = itr.next().value
            result.add(msg)
            i++
        }
        return result
    }

    /**
     * 根据范围与分组查询多个消息
     * @param startId 开始的id
     * @param group 分组
     * @param limit
     * @param inclusive 是否包含开始的id
     * @return
     */
    public fun getMessagesByRangeAndGroup(startId: Long, group: String, limit: Int, inclusive: Boolean): List<Message> {
        val groupId: Int = GroupSequence.get(group)
        // 获得按key有序的索引迭代器
        val itr = indexStore.iterator(startId, inclusive)
        var i = 0
        val result = ArrayList<Message>()
        while (itr.hasNext() && i < limit) {
            val entry = itr.next()
            val groupIds:BitSet = entry.value // 消息的分组
            if(groupIds.get(groupId)) { // 包含目标分组
                val id = entry.key
                val msg = queueStore.get(id)!! // 查消息
                result.add(msg)
                i++
            }
        }
        return result
    }

    /**
     * 根据分组查询多个消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    public override fun getMessagesByGroup(group: String, limit: Int): List<Message>{
        // 读该分组的进度
        val groupId: Int = GroupSequence.get(group)
        val startId:Long? = progressStore.get(groupId)
        // 读消息
        val result = getMessagesByRangeAndGroup(if(startId == null) 0L else startId, group, limit, false)
        if(!result.isEmpty()){
            // 保存该分组的读进度
            progressStore.put(groupId, result.last().id)
            // 同步进度文件
            progressStore.sync()
        }
        return result
    }

    /**
     * 查询单个消息
     * @param id
     * @return
     */
    public override fun getMessage(id: Long): Message? {
        return queueStore.get(id)
    }
}
