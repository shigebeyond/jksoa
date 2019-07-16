package net.jkcode.jksoa.mq

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jksoa.mq.broker.repository.LsmMqRepository
import net.jkcode.jksoa.mq.common.Message
import org.junit.Test
import java.util.*

/**
 * 测试消息存储
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class MqRepositoryTest {

    val topic = "topic1"

    val group = "group1"

    val repository = LsmMqRepository.createRepositoryIfAbsent(topic)

    @Test
    fun testSaveMessage(){
        // 新建消息
        val msgs = LinkedList<Message>()
        for(i in 0..100) {
            val msg = Message(topic, currMillis(), group)
            msgs.add(msg)
        }

        // 保存消息, 生成id
        repository.saveMessages(msgs)
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
            val msgs = repository.getMessagesByGroup(group, 2)
            println(msgs)
            println("---------------")
        }
    }

    @Test
    fun testDeleteMessage(){
        // 找一个
        val id = repository.getMessagesByRange(0, 1).first().id
        val msg = repository.getMessage(id)!!
        println("find one message: " + msg)

        // 删除
        repository.deleteMessage(msg.id)
        println("delete message: " + msg.id)

        // 读取
        val msg2 = repository.getMessage(id)
        println("find one message: " + msg2)
    }
}