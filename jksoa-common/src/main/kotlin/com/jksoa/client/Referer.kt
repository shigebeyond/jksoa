package com.jksoa.client

import com.jksoa.common.IService
import java.lang.reflect.Proxy

/**
 * 服务的引用（代理）
 *
 * @ClassName: Referer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
class Referer(public override val `interface`:Class<out IService> /* 接口类 */,
              public override val service: IService = Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(`interface`), RpcInvocationHandler(`interface`)) as IService /* 服务实例，默认是服务代理，但在服务端可指定本地服务实例 */
): IReferer() {

    companion object{

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

}