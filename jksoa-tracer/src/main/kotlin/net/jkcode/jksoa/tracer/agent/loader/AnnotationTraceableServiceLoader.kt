package net.jkcode.jksoa.tracer.agent.loader

import net.jkcode.jkmvc.common.*
import net.jkcode.jkmvc.http.httpLogger
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.tracer.agent.TraceableService
import java.lang.reflect.Modifier
import java.util.*
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * 有@TraceableService注解的类的加载器
 *
 * @author shijianhang
 * @date 2019-7-3 下午8:02:47
 */
class AnnotationTraceableServiceLoader(annotation: Class<out Annotation>) : ITraceableServiceLoader()  {

    /**
     * http配置
     */
    public val config = Config.instance("agent", "yaml")

    /**
     * 加载自定义的服务
     *    如@TraceableService注解的类
     */
    override fun load(): List<String> {
        // 获得配置的包路径
        val pcks:List<String>? = config["traceableServicePackages"]
        if(pcks.isNullOrEmpty())
            return emptyList()

        // 创建注解扫描器
        val serviceNames = LinkedList<String>()
        val c = LambdaClassScanner(){relativePath ->
            // 获得类
            val clazz = relativePath.classPath2class()
            // 检查注解
            if(clazz.getAnnotation(TraceableService::class.java) != null)
                // 用#号前缀来标识发起人的service
                serviceNames.add("#${clazz.name}")
        }

        // 扫描
        c.addPackages(pcks!!)

        return serviceNames
    }

}
