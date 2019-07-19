package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.Store
import net.jkcode.jksoa.mq.broker.repository.IMessageRepository
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
     *   key为分组名, value是读进度对应的消息id
     */
    protected lateinit var progressStore: Store<String, Long>

    /**
     * 根据范围查询多个消息
     * @param startId 开始的id
     * @param limit
     * @param inclusive 是否包含开始的id
     * @return
     */
    public override fun getMessagesByRange(startId: Long, limit: Int, inclusive: Boolean): List<Message> {
        // 获得按key有序的迭代器
        val itr = queueStore.iterator(startId, inclusive)
        var i = 0
        val result = ArrayList<Message>()
        while (itr.hasNext() && i < limit) {
            val entry = itr.next()
            result.add(entry.value)
            i++
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
        val startId:Long? = progressStore.get(group)
        // 读消息
        val result = getMessagesByRange(if(startId == null) 0L else startId, limit, false)
        if(!result.isEmpty()){
            // 保存该分组的读进度
            progressStore.put(group, result.last().id)
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
