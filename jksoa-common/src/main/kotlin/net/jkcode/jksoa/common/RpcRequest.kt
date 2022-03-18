package net.jkcode.jksoa.common

import net.jkcode.jkutil.common.generateId
import net.jkcode.jkutil.common.getSignature
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jkutil.common.getClassByName
import java.lang.reflect.Method
import java.util.HashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc请求
 *    1 远端方法调用的描述: 方法 + 参数
 *    2 invoke()主要是被job调用，实现是调用 RpcInvocationHandler.invoke(req) 来发送rpc请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
open class RpcRequest(public override val clazz: String, //服务接口类全名
                      public override val methodSignature: String, //方法签名：包含方法名+参数类型
                      public override val args: Array<Any?> = emptyArray(), //实参
                      public override val version: Int = 0, //版本
                      public override var id: Long = idSequence.getAndIncrement() // 请求标识，client实例中唯一, 注: 放到这里是因为json反序列化要设置该属性, 而不是自动生成id
): IRpcRequest, Cloneable {

    companion object{

        /**
         * id序列
         */
        protected val idSequence = AtomicLong(0)
    }

    /**
     * 附加参数
     *   初始值为null, 节省内存与序列化消耗
     */
    public override var attachments: MutableMap<String, Any?>? = null
        protected set

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray()) : this(method.getServiceClass().name, method.getSignature(), args, method.getServiceClass().remoteService?.version ?: 0)

    /**
     * 构造函数
     *   如果被调用的kotlin方法中有默认参数, 则 func.javaMethod 获得的java方法签名是包含默认参数类型的
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    /**
     * 尝试获得附加参数, 如果为null则初始化
     * @return
     */
    protected inline fun getOrInitAttachments(): MutableMap<String, Any?> {
        if (attachments == null)
            attachments = HashMap()
        return attachments!!
    }

    /**
     * 设置附加参数
     * @param key
     * @param value
     */
    public override fun putAttachment(key: String, value: Any?) {
        getOrInitAttachments()[key] = value
    }

    /**
     * 设置附加参数
     * @param data
     */
    public override fun putAttachments(data: Map<String, Any?>) {
        getOrInitAttachments().putAll(data)
    }

    /**
     * 删除附加参数
     * @param key
     * @return
     */
    public override fun removeAttachment(key: String): Any? {
        return attachments?.remove(key)
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "RpcRequest: id=$id, " + toDesc()
    }

    /**
     * 克隆对象
     * @return
     */
    public override fun clone(): Any {
        val o = super.clone() as RpcRequest
        o.id = generateId("rpc")
        return o;
    }

    /**
     * 调用
     * @return
     */
    public override fun invoke(): Any? {
        //return IRpcRequestDispatcher.instance().dispatch(this) // 无拦截器链
        //return RpcInvocationHandler.invokeRpcRequest(this) // 有拦截器链
        // 没有依赖 IRpcRequestDispatcher/RpcInvocationHandler类所在的rpc-client工程, 不能直接调用, 只能通过中间类+反射来解耦依赖
        val clazz = getClassByName("net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler")
        val invoker = clazz.kotlin.objectInstance as IRpcRequestInvoker // object
        return invoker.invoke(this)
    }
}