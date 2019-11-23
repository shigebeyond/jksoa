package net.jkcode.jksoa.mq

import net.jkcode.jkutil.common.randomString
import net.jkcode.jksoa.mq.broker.repository.lsm.LsmMessageRepository
import net.jkcode.jksoa.mq.common.Message
import org.junit.Test
import java.util.*

/**
 * 测试消息存储
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class MessageRepositoryTests {

    val topic = "topic1"

    val group = "default"

    val repository = LsmMessageRepository.createRepositoryIfAbsent(topic)

    @Test
    fun testBatchPutMessages(){
        // 新建消息
        val msgs = LinkedList<Message>()
        for(i in 0 until 100) {
            val msg = Message(topic, randomString(7).toByteArray(), group)
            msgs.add(msg)
        }

        // 保存消息, 生成id
        val ids = repository.batchPutMessages(msgs).get()
        println("保存消息: " + msgs)
    }

    @Test
    fun testGetMessagesByRange(){
        for(i in 0 until 5) {
            val msgs = repository.getMessagesByRange(i* 2L, 2)
            println(msgs)
            println("---------------")
        }
    }

    @Test
    fun testGetMessagesByGroup(){
        for(i in 0 until 5) {
            val msgs = repository.getMessagesByGroupProgress(group, 2)
            println(msgs)
            println("---------------")
        }
    }

    @Test
    fun testPutAndDeleteMessage(){
        // 新建
        val msg = Message(topic, randomString(7).toByteArray(), group)
        val id = repository.putMessage(msg).get()

        // 读取
        val msg2 = repository.getMessage(id)
        println("find one message: " + msg2)

        // 删除
        repository.finishMessage(id, group).get()
        println("delete message: " + id)

        // 读取
        val msg3 = repository.getMessage(id)
        println("find one message: " + msg3)
    }
}