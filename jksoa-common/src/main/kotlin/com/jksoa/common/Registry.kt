package com.jksoa.common

import com.jkmvc.cache.JedisFactory

/**
 * 注册中心
 *
 * @ClassName: Registry
 * @Description: 
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 12:48 PM
 */
class Registry {

    val jedis = JedisFactory.instance()

    fun register(config: ServiceConfig){
        jedis.lpush("c:${config.path}".toByteArray())
        jedis.psubscribe
    }

    fun unregister(config: ServiceConfig){

    }

    fun unregister(config: String){

    }

    fun addConnectedListener(listener:(Boolean) -> Unit){

    }

    fun addChildListener(listener:(Boolean) -> Unit){

    }


}