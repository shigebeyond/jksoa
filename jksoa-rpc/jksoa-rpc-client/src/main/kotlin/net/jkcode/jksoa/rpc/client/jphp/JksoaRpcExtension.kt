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
        return arrayOf("jksoa\\rpc")
    }

    override fun onRegister(scope: CompileScope) {
        registerClass(scope, WrapJavaReferer::class.java)
        registerClass(scope, WrapPhpReferer::class.java)
        registerClass(scope, WrapCompletableFuture::class.java)
    }

}