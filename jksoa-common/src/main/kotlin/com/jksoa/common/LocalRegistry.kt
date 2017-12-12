package com.jksoa.common

import java.net.URL


/**
 * 基于本地内存的注册中心
 *
 * @ClassName: LocalRegistry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
class LocalRegistry :IRegistry{


    override fun register(name: String, url: URL) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregister(name: String, url: URL) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addConnectedListener(listener: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addChildListener(listener: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}