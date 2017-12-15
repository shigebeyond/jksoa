package com.jksoa.server

import com.jkmvc.common.ClassScanner
import com.jkmvc.common.Config
import com.jksoa.common.IService
import com.jksoa.server.IProvider
import java.io.File
import java.lang.reflect.Modifier

/**
 * 加载服务提供者
 *
 * @ClassName:ServiceLoader
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 10:27 AM
 */
object ProviderLoader: ClassScanner(), IProviderLoader {

    /**
     * 服务端配置
     */
    private val config = Config.instance("server", "yaml")

    /**
     * 服务提供者缓存
     *   key为服务名，即接口类全名
     *   value为提供者
     */
    private val providers:MutableMap<String, Provider> = HashMap()

    init{
        // 加载配置的包路径
        val pcks:List<String>? = config["servicePackages"]
        if(pcks != null)
            addPackages(pcks)
    }

    /**
     * 收集Service类
     *
     * @param relativePath 类文件相对路径
     */
    public override fun collectClass(relativePath: String): Unit{
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
            val serviceName = provider.`interface`.name
            providers[serviceName] = provider
        }
    }

    /**
     * 根据服务名来获得服务提供者
     *
     * @param name
     * @return
     */
    public override fun getProvider(name: String): IProvider?{
        return providers[name]
    }

    /**
     * 获得所有的服务提供者
     * @return
     */
    public override fun getProviders(): Collection<IProvider> {
        return providers.values.toSet()
    }
}