package net.jkcode.jksoa.mq.broker.repository.lsm

import com.indeed.lsmtree.core.Store
import net.jkcode.jksoa.mq.common.Message
import org.apache.commons.collections.iterators.AbstractIteratorDecorator
import java.util.*

/**
 * 消息id = topic + 消息id
 */
typealias MessageId = Pair<String, Long>

/**
 * 消息id实体的迭代器 转 消息id迭代器
 */
class MessageIdIterator(protected val entryItr: Iterator<Store.Entry<Long, List<MessageId>>>) : Iterator<MessageId> {

    protected var idItr: Iterator<MessageId> = Collections.emptyIterator()

    /**
     * 尝试切换到下一个id迭代器, 并返回hasNext()结果
     * @return
     */
    protected fun trySwitchNextIdItr(): Boolean {
        while (!idItr.hasNext()) {
            // 切换到下一个id迭代器
            if (!entryItr.hasNext())
                return false

            idItr = entryItr.next().value.iterator()
        }
        return true
    }

    /**
     * 是否有下一个消息
     * @return
     */
    public override fun hasNext(): Boolean {
        return trySwitchNextIdItr()
    }

    /**
     * 获得下一个消息
     * @return
     */
    public override fun next(): MessageId {
        while(trySwitchNextIdItr())
            return idItr.next()

        throw NoSuchElementException()
    }

}

/**
 * 消息id实体的迭代器 转 消息迭代器
 */
class MessageIterator(protected val idItr: MessageIdIterator) : AbstractIteratorDecorator(idItr) {

    /**
     * 获得下一个消息
     * @return
     */
    public override fun next(): Message? {
        val (topic, id) = super.next() as MessageId

        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(topic)
        // 查询消息
        return repository.getMessage(id)
    }

}