package com.jksoa.job

import com.jksoa.client.Referer
import com.jksoa.example.IEchoService
import org.junit.Test
import kotlin.reflect.jvm.javaMethod

class JobTests{

    @Test
    fun testJob(){
        val job = Job(IEchoService::echo, 3){ i ->
            arrayOf("第${i}个参数")
        }

        val service = Referer.getRefer<IEchoService>()
    }

}





