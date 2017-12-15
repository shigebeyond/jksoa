package com.jksoa.protocol.rmi

import com.jkmvc.common.isSuperClass
import com.jksoa.common.IService
import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.Url
import com.jksoa.protocol.IConnection
import com.jksoa.server.ServiceException
import java.lang.reflect.Method
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap
import javax.naming.InitialContext

/**
 * rmi链接
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
class RmiConnection(url: Url): IConnection(url){

    companion object{
        /**
         * 方法
         */
        private val methods: ConcurrentHashMap<String, HashMap<String, Method>> = ConcurrentHashMap()
    }

    /**
     * 命名空间
     */
    protected val namingContext = InitialContext()

    /**
     * 远端对象
     */
    protected var remoteObject: Any = namingContext.lookup(url.toString(false))

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: Request): Response {
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        namingContext.close()
    }

}
