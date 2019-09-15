package net.jkcode.jksoa.common.invocation

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jkmvc.singleton.BeanSingletons
import java.io.Serializable
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 本地方法调用的描述: 方法 + 参数
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
open class Invocation(public override val clazz: String, /* 服务接口类全名 */
                      public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                      public override val args: Array<Any?> = emptyArray() /* 实参 */
): IInvocation, Serializable {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray()) : this(method.declaringClass.name, method.getSignature(), args)

    /**
     * 构造函数
     *   如果被调用的kotlin方法中有默认参数, 则 func.javaMethod 获得的java方法签名是包含默认参数类型的
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    /**
     * 被调用的bean
     */
    protected val bean:Any
        //= BeanSingletons.instance(clazz) // 不能引用(包含递延引用), 否则会被序列化, 如在tcc场景下需要对confirm/cancel方法调用进行序列化
        get() = BeanSingletons.instance(clazz)

    /**
     * 调用
     * @return
     */
    public override fun invoke(): Any? {
        return method.invoke(bean, *args)
    }
}