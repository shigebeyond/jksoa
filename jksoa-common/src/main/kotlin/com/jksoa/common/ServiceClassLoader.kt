package com.jksoa.common

import com.jkmvc.common.ClassScanner
import com.jkmvc.common.IConfig
import com.jkmvc.common.isSuperClass
import java.io.File
import java.lang.reflect.Modifier

/**
 * 加载服务类
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
abstract class ServiceClassLoader<T: IServiceClass> : ClassScanner() {

    /**
     * 服务类缓存
     *   key为服务标识，即接口类全名
     *   value为服务类元数据
     */
    protected val serviceClasses:MutableMap<String, T> = HashMap()

    /**
     * 根据服务标识来获得服务类元数据
     *
     * @param name
     * @return
     */
    public open fun get(name: String): T?{
        return serviceClasses[name]
    }

    /**
     * 获得所有的服务类元数据
     * @return
     */
    public open fun getAll(): Collection<T> {
        return serviceClasses.values.toSet()
    }

    /**
     * 收集Service类
     *
     * @param relativePath 类文件相对路径
     */
    public override fun collectClass(relativePath: String): Unit {
        // 过滤service的类文件
//        if(!relativePath.endsWith("Service.class"))
//            return

        // 获得类名
        val className = relativePath.substringBefore(".class").replace(File.separatorChar, '.')
        // 获得类
        val clazz = Class.forName(className) as Class<IService>
        // 过滤service子类
        if(IService::class.java.isSuperClass(clazz) /* 继承IService */){
            // 创建服务类元数据
            val serviceClass = collectServiceClass(clazz)
            // 缓存服务提供者，key是服务标识，即接口类全名
            if(serviceClass != null)
                serviceClasses[serviceClass.serviceId] = serviceClass
        }
    }

    /**
     * 收集服务类元数据
     *
     * @param clazz
     * @return
     */
    public abstract fun collectServiceClass(clazz: Class<IService>): T?
}