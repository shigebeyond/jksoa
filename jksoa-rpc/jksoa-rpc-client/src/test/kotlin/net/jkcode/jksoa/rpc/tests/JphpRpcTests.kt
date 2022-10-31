package net.jkcode.jksoa.rpc.tests

import net.jkcode.jksoa.rpc.client.jphp.IP2pService
import net.jkcode.jkutil.common.getFullSignature
import net.jkcode.jphp.ext.JphpLauncher
import org.junit.Test

class JphpRpcTests {

    @Test
    fun testRpc(){
        val lan = JphpLauncher
        lan.run("src/test/resources/rpc.php")
    }

    @Test
    fun testP2pServicePhp(){
        val lan = JphpLauncher
        val phpFile = Thread.currentThread().contextClassLoader.getResource("jphp/IP2pService.php").path
        val clazz = lan.loadFrom(phpFile).classes.first()
        print(clazz)
    }

    @Test
    fun testP2pServiceMethodSign(){
        for(f in IP2pService::class.java.methods){
            println(f.getFullSignature())
        }
    }


}