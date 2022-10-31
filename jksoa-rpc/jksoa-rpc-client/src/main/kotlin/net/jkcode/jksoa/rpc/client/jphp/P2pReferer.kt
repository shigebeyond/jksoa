package net.jkcode.jksoa.rpc.client.jphp

import net.jkcode.jphp.ext.JphpLauncher
import php.runtime.env.Environment
import php.runtime.reflection.ClassEntity
import java.util.concurrent.ConcurrentHashMap

/**
 * php服务的php引用: php client调用php service，通过某个java rpc方法
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 *   实现注意:
 *   1. 因为没有java接口类，因此不支持 service/getMethod()
 *   2. 也不能使用 RefererLoader 来加载与获得服务引用, 因为 RefererLoader 要扫描java服务接口类
 *   3. 私有构造函数, 只能通过 P2pReferer.getOrPutRefer(phpClazzName, env) 来获得实例
 *
 *   调用注意:
 *   通过java rpc方法来代理调用server端的php代码
 *   java rpc服务必须实现接口 IP2pService
 *
 *   使用:
 *   $ref = new P2pReferer('net.jkcode.jksoa.rpc.example.IP2pTestService');
 *   $ret = $ref->callPhpFunc('Test::sayHi', ['shi']);
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-10-14 9:52 AM
 */
class P2pReferer protected constructor(
    env: Environment,
    serviceId: String // java rpc服务, 必须实现接口 IP2pService, 如 net.jkcode.jksoa.rpc.example.IP2pTestService
): IPhpReferer(env, serviceId){

    companion object {

        /**
         * 单例池
         */
        protected var insts: ConcurrentHashMap<String, P2pReferer> = ConcurrentHashMap();

        /**
         * 根据服务接口，来获得服务引用
         *   有缓存
         * @param serviceId
         * @return
         */
        internal fun getOrCreateRefer(serviceId: String, env: Environment): P2pReferer {
            return insts.getOrPut(serviceId){
                P2pReferer(env, serviceId)
            }
        }

        /**
         * 引用 IP2pService.php 中的类, 用于辅助构建 P2pReferer.refererMethods
         */
        protected val p2pServiceClass: ClassEntity by lazy{
            val lan = JphpLauncher
            val phpFile = Thread.currentThread().contextClassLoader.getResource("jphp/IP2pService.php").path
            lan.loadFrom(phpFile).classes.first()
        }

    }

    init {
        // 构建引用方法
        refererMethods = p2pServiceClass.methods.values.associate { method ->
            method.name to PhpRefererMethod(this, method)
        }
    }

}
