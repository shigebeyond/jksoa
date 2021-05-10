package net.jkcode.jksoa.dtx.mq

import net.jkcode.jkmvc.db.Db
import org.junit.Test

class MqTransactionTests{

    val topic = "new_user"

    @Test
    fun testAddMq(){
        val db = Db.instance()
        // 本地事务
        db.transaction {
            // 执行业务sql
            val uid = db.execute("insert into user(name, age) values(?, ?)" /*sql*/, listOf("shi", 1)/*参数*/, "id"/*自增主键字段名，作为返回值*/) // 返回自增主键值
            println("插入user表：" + uid)

            // 添加事务消息
            MqTransactionManager.addMq(topic, "new user: $uid".toByteArray(), "new user", uid.toString())
        }

        Thread.sleep(100000)
    }



}