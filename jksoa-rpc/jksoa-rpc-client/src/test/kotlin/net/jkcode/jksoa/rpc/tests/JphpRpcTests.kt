package net.jkcode.jksoa.rpc.tests

import net.jkcode.jphp.ext.JphpLauncher
import net.jkcode.jphp.ext.WrapJavaObject
import org.junit.Test

class JphpRpcTests {

    @Test
    fun testJphpLauncher(){
        val lan = JphpLauncher.instance()
        lan.run("src/test/resources/rpc.php")
    }
}