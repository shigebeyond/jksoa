package net.jkcode.jksoa.rpc.tests.job

import net.jkcode.jkjob.jobLogger

/**
 * 冗余job项目的代码，因为发现怎么引用都引用不了
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 10:08 PM
 */
class LocalBean {

    public fun sayHi(name: String): String{
        return "Greeting, $name"
    }

}