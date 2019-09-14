package net.jkcode.jksoa.dtx.tcc

import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 7:07 PM
 */
class TccTransactionTest {

    @TccMethod("", "", "test", "")
    fun handleBusiness(id: Int){
        println("handleBusiness: " + id)
    }

    @Test
    fun testTcc(){
        handleBusiness(1)
    }
}