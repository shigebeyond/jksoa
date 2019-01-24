package com.jksoa.job.job.local

import com.jkmvc.common.getConstructorOrNull
import com.jkmvc.common.getMethodBySignature
import com.jkmvc.common.getOrPutOnce
import com.jksoa.common.IInvocationMethod
import com.jksoa.job.JobException
import com.jksoa.job.job.BasicJob
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 调用bean方法的作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 8:41 PM
 */
abstract class BasicLpcJob : BasicJob(), IInvocationMethod {

    companion object {

        /**
         * 缓存的bean对象
         */
        private val beans: ConcurrentHashMap<String, Any> = ConcurrentHashMap();
    }

    /**
     * 被调用的bean
     *   由于clazz属性在子类初始化，递延引用
     */
    protected val bean:Any by lazy {
        beans.getOrPutOnce(clazz){
            val c = Class.forName(clazz) // ClassNotFoundException
            // 检查bean类的默认构造函数
            if(c.getConstructorOrNull() == null)
                throw JobException("Bean Class [$c] has no no-arg constructor") // 无默认构造函数
            // 创建bean
            c.newInstance()
        }
    }

    /**
     * 被调用的方法
     *   由于clazz属性在子类初始化，递延引用
     */
    protected val method: Method by lazy {
        val c = Class.forName(clazz) // ClassNotFoundException
        val m = c.getMethodBySignature(methodSignature)
        if(m == null)
            throw JobException("Bean Class [$clazz] has no method [$methodSignature]") // 无函数
        m!!
    }
}