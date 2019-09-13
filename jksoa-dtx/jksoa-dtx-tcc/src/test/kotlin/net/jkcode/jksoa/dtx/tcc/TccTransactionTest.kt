package net.jkcode.jksoa.dtx.tcc

import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 7:07 PM
 */
class TccTransactionTest {

    @TccMethod("", "", "test", "")
    fun tccMethod(id: Int){
        println(id)
    }

    @Test
    fun testTcc(){
        tccMethod(1)
    }
}