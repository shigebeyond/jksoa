package net.jkcode.jksoa.job

import net.jkcode.jksoa.job.cronjob.CronJobLaucher
import org.junit.Test

/**
 * 作业表达式解析
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:27 PM
 */
class CronJobLauncherTests: BaseTests() {

    @Test
    fun testLaunch(){
        try {
            val cronJobExpr = "0/10 * * * * ? :lpc net.jkcode.jksoa.job.LocalBean echo(String) (\\\"测试消息\\\")"
            //val cronJobExpr = "0/10 * * * * ? :rpc net.jkcode.jksoa.example.ISystemService echo(String) (\"测试消息\")"
            trigger = CronJobLaucher.lauch(cronJobExpr)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}