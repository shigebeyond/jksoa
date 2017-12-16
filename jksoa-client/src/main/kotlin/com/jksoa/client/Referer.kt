package com.jksoa.client

import com.jkmvc.common.ShutdownHook
import com.jksoa.common.IService
import com.jksoa.registry.IRegistry
import com.jksoa.registry.zk.ZkRegistry
import java.lang.reflect.Proxy

/**
 * 服务的引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
class Referer(public override val `interface`:Class<out IService> /* 接口类 */,
              public override val service: IService = createProxy(`interface`) /* 服务实例，默认是服务代理，但在服务端可指定本地服务实例 */
): IReferer() {

    companion object{

        /**
         * 注册中心
         * TODO: 支持多个配置中心, 可用组合模式
         */
        public val registry: IRegistry = ZkRegistry

        /**
         * 创建服务代理
         */
        public fun createProxy(intf: Class<out IService>): IService {
            return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler(intf)) as IService
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @param clazz
         * @return
         */
        public fun <T: IService> getRefer(clazz: Class<T>): T {
            val referer = RefererLoader.get(clazz.name)
            if(referer == null)
                throw RpcException("未加载服务: " + clazz.name);
            return referer.service as T
        }

        /**
         * 根据服务接口，来获得服务引用
         *
         * @return
         */
        public inline fun <reified T: IService> getRefer(): T {
            return getRefer(T::class.java)
        }
    }

    init {
        // 监听服务变化
        registry.subscribe(serviceName, Broker)

        // 要关闭
        ShutdownHook.addClosing(this)
    }

    /**
     * 取消监听服务变化
     */
    public override fun close() {
        registry.unsubscribe(serviceName, Broker)
    }
}
