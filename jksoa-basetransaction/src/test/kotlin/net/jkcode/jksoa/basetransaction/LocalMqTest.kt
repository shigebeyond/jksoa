package net.jkcode.jksoa.basetransaction

import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.basetransaction.localmq.LocalMqManager

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 7:07 PM
 */
class LocalMqTest {

    fun testLocalMq(){
        val db = Db.instance()
        // 本地事务
        db.transaction {
            // 执行业务sql
            val uid = db.execute("insert into user(name, age) values(?, ?)" /*sql*/, listOf("shi", 1)/*参数*/, "id"/*自增主键字段名，作为返回值*/) // 返回自增主键值
            println("插入user表：" + uid)

            // 添加本地消息
            LocalMqManager.addLocalMq("new user", uid.toString(), "new_user", )

        }

    }
}