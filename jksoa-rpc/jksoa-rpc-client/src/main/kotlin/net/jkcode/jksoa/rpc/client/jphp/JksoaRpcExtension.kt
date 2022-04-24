package net.jkcode.jksoa.rpc.client.jphp

import php.runtime.env.CompileScope
import php.runtime.ext.support.Extension
import org.develnext.jphp.zend.ext.ZendExtension

class JksoaRpcExtension : Extension() {

    companion object {
        const val NS = "php\\jksoa\\rpc"
    }

    override fun getStatus(): Status {
        return Status.EXPERIMENTAL
    }

    override fun getRequiredExtensions(): Array<String?>? {
        return arrayOf(
                ZendExtension::class.java.getName()
        )
    }

    override fun getPackageNames(): Array<String> {
        return arrayOf("jkmvc\\http")
    }

    override fun onRegister(scope: CompileScope) {
        registerClass(scope, PReferer::class.java)
        registerClass(scope, WrapCompletableFuture::class.java)
    }

}