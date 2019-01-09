package com.jksoa.job

import com.jksoa.example.IExampleService
import org.junit.Test

class JobTests{

    @Test
    fun testJob(){
        val job = Job(IExampleService::sayHi, 3){ i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val distributor = JobDistributor()
        distributor.distribute(job)
    }

}





