package com.jksoa.job

import com.jksoa.example.ISystemService
import com.jksoa.example.SystemService
import com.jksoa.job.job.local.LpcJob
import com.jksoa.job.job.local.ShardingLpcJob
import com.jksoa.job.job.remote.RpcJob
import com.jksoa.job.job.remote.ShardingRpcJob
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

            trigger = CronJobLaucher.lauch("0/10 * * * * ? :lpc com.jksoa.example.SystemService ping() ()")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}