package com.jksoa.tests

import com.jksoa.registry.zk.ZkRegistry
import com.jksoa.registry.zk.common.ZkClientFactory
import org.I0Itec.zkclient.ZkClient
import org.junit.Test

/**
 * @ClassName: ClientTests
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RegisterTests {

    @Test
    fun testZk(){
        val zkClient: ZkClient = ZkClientFactory.instance()
        val dir = "a"
        if (!zkClient.exists(dir))
            zkClient.createPersistent(dir, true)

        // 创建节点
        zkClient.createEphemeral("a/b", "123")
    }

}