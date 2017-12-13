package com.jksoa.common

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.travel
import java.io.File
import java.lang.reflect.Modifier
import java.util.*

/**
 * 加载服务提供者
 *
 * @ClassName:ServiceLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object ServiceLoader : IServiceLoader {

    /**
     * soa配置
     */
    public val config = Config.instance("soa", "yaml")

    /**
     * 自动扫描的包
     */
    private val packages:MutableList<String> = LinkedList<String>();

    /**
     * 服务提供者缓存
     *   key为服务名，即接口类全名
     *   value为提供者
     */
    private val services:MutableMap<String, Provider> by lazy {
        scan()
    }

    init{
        // 加载配置的包路径
        val pcks:List<String>? = config["servicePackages"]
        if(pcks != null)
            addPackages(pcks)
    }

    /**
     * 添加单个包
     * @param pck 包名
     * @return
     */
    override fun addPackage(pck:String): ServiceLoader {
        soaLogger.info("添加service包: $pck")
        packages.add(pck)
        return this;
    }

    /**
     * 添加多个包
     * @param pcks 包名
     * @return
     */
    override fun addPackages(pcks:Collection<String>): ServiceLoader {
        soaLogger.info("添加service包: $pcks")
        packages.addAll(pcks)
        return this;
    }

    /**
     * 扫描指定包下的服务提供者
     * @return
     */
    override fun scan(): MutableMap<String, Provider> {
        val result:MutableMap<String, Provider> = HashMap<String, Provider>()

        // 获得类加载器
        val cld = Thread.currentThread().contextClassLoader

        // 遍历包来扫描
        for (pck in packages){
            // 获得该包的所有资源
            val path = pck.replace('.', '/')
            val urls = cld.getResources(path)
            // 遍历资源
            for(url in urls){
                // 遍历某个资源下的文件
                url.travel { relativePath, isDir ->
                    // 收集服务提供者
                    collectService(relativePath, result)
                }
            }
        }
        
        return result;
    }

    /**
     * 收集服务提供者
     *
     * @param relativePath
     * @param isDir
     * @return
     */
    private fun collectService(relativePath: String, result: MutableMap<String, Provider>){
        // 过滤service的类文件
        if(!relativePath.endsWith("service.class"))
            return

        // 获得类名
        val className = relativePath.substringBefore(".class").replace(File.separatorChar, '.')
        // 获得类
        val clazz = Class.forName(className) as Class<IService>
        val modifiers = clazz.modifiers
        // 过滤service子类
        val base = IService::class.java
        if(base != clazz && base.isAssignableFrom(clazz) /* 继承IService */ && !Modifier.isAbstract(modifiers) /* 非抽象类 */ && !Modifier.isInterface(modifiers) /* 非接口 */){
            // 构建服务提供者
            val provider = Provider(clazz)
            // 缓存服务提供者，key是服务名，即接口类全名
            for(intf in provider.interfaces)
                result.put(intf.name, provider)
        }
    }

    /**
     * 获得服务提供者
     *
     * @param name
     * @return
     */
    public override fun getService(name: String): Provider?{
        return services[name]
    }

    /**
     * 获得服务提供者
     *
     * @return
     */
    public inline fun <reified T:IService> getService(): T?{
        val intf = T::class.java
        return getService(intf.name) as T
    }
}