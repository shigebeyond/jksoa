package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.generateId
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.annotation.remoteService
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc请求
 *    远端方法调用的描述: 方法 + 参数
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
     */
    public override val attachments: MutableMap<String, Any?> = HashMap()

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
        val clazz = Class.forName("net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler")
        val invoker = clazz.kotlin.objectInstance as IRpcRequestInvoker // object
        return invoker.invoke(this)
    }
}