package com.jksoa.common

import com.jkmvc.common.ClassScanner
import com.jkmvc.common.classPath2class
import com.jkmvc.common.isSuperClass

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
     * 加载服务
     */
    public abstract fun load()

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
        if(!relativePath.endsWith("Service.class"))
            return
        val clazz = relativePath.classPath2class() as Class<IService>

        // 过滤service子类
        if(IService::class.java.isSuperClass(clazz) /* 继承IService */)
            addClass(clazz, true) // 收集类
    }

    /**
     * 收集Service类
     *
     * @param clazz 类
     * @param registerable 是否注册
     */
    public fun addClass(clazz: Class<out IService>, registerable: Boolean) {
        // 创建服务类元数据
        val serviceClass = createServiceClass(clazz, registerable)
        // 缓存服务提供者，key是服务标识，即接口类全名
        if (serviceClass != null)
            //serviceClasses[clazz.name] = serviceClass // wrong: key是接口类, 而不是当前类
            serviceClasses[serviceClass.serviceId] = serviceClass
    }

    /**
     * 创建服务类元数据
     *
     * @param clazz
     * @param registerable 是否注册
     * @return
     */
    protected abstract fun createServiceClass(clazz: Class<out IService>, registerable: Boolean): T?
}