package net.jkcode.jksoa.rpc.client.connection.pooled

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.IRpcClient
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject

/**
 * 池化的连接的工厂
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:03 AM
 */
class PooledConnectionFactory(public val url: Url /* 服务端地址 */) : BasePooledObjectFactory<IConnection>() {

    /**
     * 创建连接
     */
    public override fun create(): IConnection {
        // 根据rpc协议获得对应的client
        val client = IRpcClient.instance(url.protocol)
        // 连接server
        return client.connect(url)
    }

    /**
     * 包装连接
     */
    public override fun wrap(conn: IConnection): PooledObject<IConnection> {
        return DefaultPooledObject<IConnection>(conn)
    }

    /**
     * 销毁连接
     */
    public override fun destroyObject(p: PooledObject<IConnection>) {
        val conn = p.getObject()
        conn.close()
    }

    /**
     * 校验连接
     */
    public  override fun validateObject(p: PooledObject<IConnection>): Boolean {
        val conn = p.getObject()
        return conn.isValid()
    }


}
