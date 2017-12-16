package com.jksoa.tests

import com.jksoa.common.Url
import org.junit.Test

/**
 * @ClassName: ClientTests
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MyTests {

    @Test
    fun testUrl(){
        val url = Url("mysql://127.0.0.1:3306/test?username=root&password=root")
        //val url = URL("mysql://127.0.0.1:3306/?username=root&password=root")
        //val url = URL("mysql://127.0.0.1:3306?username=root&password=root")
        //val url = URL("mysql://127.0.0.1?username=root&password=root")
        //val url = URL("mysql://127.0.0.1")
        println(url)
    }
}