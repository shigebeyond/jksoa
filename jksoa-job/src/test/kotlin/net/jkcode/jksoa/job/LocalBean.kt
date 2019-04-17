package net.jkcode.jksoa.job

import net.jkcode.jksoa.common.serverLogger

/**
 * 测试的bean类
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 10:08 PM
 */
class LocalBean {

    public fun echo(msg: String): String{
        jobLogger.debug("调用本地bean的方法: echo(\"{}\")", msg)
        return msg
    }

}