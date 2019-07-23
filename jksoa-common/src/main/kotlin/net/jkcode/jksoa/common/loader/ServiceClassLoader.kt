package net.jkcode.jksoa.common.loader

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.common.annotation.remoteService
import kotlin.collections.set

/**
 * 加载服务类
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
abstract class ServiceClassLoader<T: IServiceClass>(protected val isProvider: Boolean /* 是否是加载服务提供者 */) : ClassScanner() {

    /**
     * 启动者
     */
    protected val starter = AtomicStarter()

    /**
     * 服务类缓存
     *   key为服务标识，即接口类全名
     *   value为服务类元数据
     */
    protected val serviceClasses:MutableMap<String, T> = HashMap()

    /**
     * 配置
     */
    protected abstract val config: IConfig

    /**
     * 扫描加载服务, 可多次调用, 只有第一次有效
     *   如果是ProviderLoader, 则在server启动时调用
     *   如果是RefererLoader, 则要延迟到 RcpRequestDispatcher初始化/RefererLoader.get() 时才调用
     *      主要是为了先调用 ProviderLoader.load() 调用, 以便能添加本地服务, 因此要延迟调用
     */
    public fun load(){
        commonLogger.debug(" ------ load service: {} ------ ", this.javaClass)
        starter.startOnce {
            // 系统的service包
            addPackage("net.jkcode.jksoa.service")
            // 用户定义的service包
            addPackages(config["servicePackages"]!!)
        }
    }

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
        val clazz = relativePath.classPath2class()

        // 过滤service注解: 接口声明注解 @RemoteService
        val isRemoteService: Boolean = if(isProvider)
                                            clazz.interfaces.any { it.remoteService != null } // 自身是实现类
                                        else
                                            clazz.remoteService != null // 自身是接口
        if(isRemoteService)
            addClass(clazz) // 收集类
    }

    /**
     * 收集Service类
     *
     * @param clazz 类
     */
    public fun addClass(clazz: Class<*>) {
        // 创建服务类元数据
        val serviceClass = createServiceClass(clazz)
        // 缓存服务提供者，key是服务标识，即接口类全名
        if (serviceClass != null)
            //serviceClasses[clazz.name] = serviceClass // wrong: key是接口类, 而不是当前类
            serviceClasses[serviceClass.serviceId] = serviceClass
    }

    /**
     * 创建服务类元数据
     *
     * @param clazz 对ProviderLoader是实现类, 对RefererLoader是接口类
     * @return
     */
    protected abstract fun createServiceClass(clazz: Class<*>): T?
}