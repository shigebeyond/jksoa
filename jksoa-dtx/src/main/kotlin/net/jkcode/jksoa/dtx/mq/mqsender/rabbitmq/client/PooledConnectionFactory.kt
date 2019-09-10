package net.jkcode.jksoa.dtx.mq.mqsender.rabbitmq.client

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import net.jkcode.jkmvc.common.IConfig
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject

/**
 * 池化的连接的工厂
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-23 11:03 AM
 */
internal class PooledConnectionFactory(public val config: IConfig) : BasePooledObjectFactory<Connection>() {

    /**
     * rabbitmq原生的连接工厂
     */
    protected val factory: ConnectionFactory by lazy{
        // 连接工厂
        val factory = ConnectionFactory()
        factory.host = config["host"]
        factory.port = config["port"]!!
        factory.virtualHost = config["vhost"]
        factory.username = config["username"]
        factory.password = config["password"]
        factory
    }

    /**
     * 创建连接
     */
    public override fun create(): Connection {
        return factory.newConnection()
    }

    /**
     * 包装连接
     */
    public override fun wrap(conn: Connection): PooledObject<Connection> {
        return DefaultPooledObject<Connection>(conn)
    }

    /**
     * 销毁连接
     */
    public override fun destroyObject(p: PooledObject<Connection>) {
        val conn = p.getObject()
        conn.close()
    }

    /**
     * 校验连接
     */
    public override fun validateObject(p: PooledObject<Connection>): Boolean {
        val conn = p.getObject()
        return conn.isOpen
    }


}
